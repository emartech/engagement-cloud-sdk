.PHONY: build build-pipeline check-env clean create-apks help lint test test-web test-android test-android-firebase test-jvm prepare-spm
.DEFAULT_GOAL := help
SHELL := /bin/bash

ifneq (,$(wildcard .env))
include .env
export
endif

REQUIRED_VARS := $(shell cat .env.example | sed 's/=.*//' | xargs)
check-env:
	@MISSING_VARS=""; \
	for var in $(REQUIRED_VARS); do \
		if [ -z "$${!var+x}" ]; then \
			MISSING_VARS="$$MISSING_VARS $$var"; \
		fi; \
	done; \
	if [ -n "$$MISSING_VARS" ]; then \
		echo "Missing environment variables:$$MISSING_VARS"; \
		echo "Please set them in your .env file or as system environment variables. Check https://secret.emarsys.net/cred/detail/18243/"; \
		exit 1; \
	fi
help: ## Show this help
	@echo "Targets:"
	@fgrep -h "##" $(MAKEFILE_LIST) | grep ":" | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/\(.*\):.*##[ \t]*/    \1 ## /' | sort | column -t -s '##'
	@echo

build: check-env ## build project with yarn actualization
	@./gradlew kotlinUpgradeYarnLock build


build-pipeline: check-env ## compile and build all modules for all platforms
	@./gradlew :engagement-cloud-sdk:build \
			 	:engagement-cloud-sdk:javaPreCompileRelease \
			   	-x :engagement-cloud-sdk:compileTestDevelopmentExecutableKotlinJs \
			   	-x :engagement-cloud-sdk:test \
			   	-x :engagement-cloud-sdk:lint \
			   	-x :composeApp:build \
			   	-x :composeApp:jsPackageJson \
			   	-x :composeApp:jsTestPackageJson \
			   	-x :composeApp:jsPublicPackageJson \
			   	-x :composeApp:jsTestPublicPackageJson \
			   	-x :engagement-cloud-sdk:jsTestPackageJson \
			   	-x :engagement-cloud-sdk:jsPublicPackageJson \
			   	-x :engagement-cloud-sdk:jsTestPublicPackageJson \
			   	-x :engagement-cloud-sdk:jsPackageJson \
			   	-x :rootPackageJson \
			   	-x :kotlinNodeJsSetup \
			   	-x :kotlinNpmCachesSetup \
			   	-x :kotlinStoreYarnLock \
			   	-x :kotlinRestoreYarnLock \
			   	-x :kotlinYarnSetup \
			   	-x :kotlinNpmInstall

clean: check-env ## clean all build artifacts
	@./gradlew clean

create-apks: check-env ## create apks for testing
	@./gradlew assembleAndroidTest

test: check-env test-android test-web test-sdk-loader test-jvm test-ios ## run common tests on all platforms (jvm,web,android, ios)
	@./gradlew :engagement-cloud-sdk:allTests -x :composeApp:test

build-web: check-env ## run tests on web
	@./gradlew jsBrowserProductionWebpack \
 			-x :composeApp:jsBrowserProductionWebpack

test-web: check-env ## run tests on web
	@./gradlew jsBrowserTest \
 			-x :composeApp:jsBrowserTest

test-sdk-loader: check-env ## run sdk loader test
	@./gradlew sdkLoaderTest

build-android: check-env ##
	@./gradlew assembleRelease -x :composeApp:assembleRelease

test-android: check-env ## run Android tests for all modules
	@./gradlew connectedAndroidTest -x :composeApp:connectedAndroidTest

build-ios: check-env ## build iOS
	@./gradlew compileKotlinIosArm64

build-ios-all-archtypes: check-env ## build iOS
	@./gradlew linkReleaseFrameworkIosArm64 linkReleaseFrameworkIosX64 linkReleaseFrameworkIosSimulatorArm64 \
	        -x :composeApp:linkReleaseFrameworkIosArm64 \
	        -x :composeApp:linkReleaseFrameworkIosX64 \
	        -x :composeApp:linkReleaseFrameworkIosSimulatorArm64

test-ios: check-env ## run iOS tests
	@./gradlew iosSimulatorArm64Test -x :composeApp:iosSimulatorArm64Test

test-android-firebase: check-env ## run Android Instrumented tests on Firebase Test Lab
	@gcloud firebase test android run \
       --type instrumentation \
       --app ./composeApp/build/outputs/apk/androidTest/debug/composeApp-debug-androidTest.apk \
       --test ./emarsys-sdk/build/outputs/apk/androidTest/debug/emarsys-sdk-debug-androidTest.apk \
       --device model=f2q,version=30,locale=en,orientation=portrait  \
       --device model=a51,version=31,locale=en,orientation=portrait \
       --device model=bluejay,version=32,locale=en,orientation=portrait \
       --device model=b4q,version=33,locale=en,orientation=portrait \
       --client-details matrixLabel="Unified SDK"

lint: check-env ## run Android Instrumented tests
	@./gradlew :engagement-cloud-sdk:lint -x :composeApp:lint

prepare-spm: check-env ## prepare swift package manager package for iOS
	@./gradlew spmDevBuild && \
	cp -f "./iosReleaseSpm/Package.swift" "./Package.swift" && \
	echo "Swift Package is prepared. To use it as a local dependency add the project in Xcode at the Package Dependencies section"

prepare-release: check-env ## setup prerequisites for release
	@./gradlew base64EnvToFile -PpropertyName=SONATYPE_SIGNING_SECRET_KEY_RING_FILE_BASE64 -Pfile=./secring.asc.gpg

release: check-env prepare-release
	@./gradlew assembleRelease && ./gradlew publishToMavenCentral

release-locally: check-env prepare-release ## release to mavenLocal
	@./gradlew assembleRelease && ./gradlew publishToMavenLocal