.PHONY: build build-pipeline check-env clean create-apks help lint test test-web test-android test-android-firebase test-jvm link-ios ios-framework lipo-merge copy-lipo-output remove-temp-lipo temp-lipo-dir remove-old-output
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
	@./gradlew :emarsys-sdk:build \
				:emarsys-sdk:javaPreCompileRelease \
		 		-x :emarsys-sdk:compileTestDevelopmentExecutableKotlinJs \
				-x :emarsys-sdk:test \
		  		-x :emarsys-sdk:lint \
			    -x :emarsys-sdk:testDebugUnitTest \
			    -x :emarsys-sdk:testReleaseUnitTest \
			 	-x :emarsys-sdk:jsBrowserTest \
			 	-x :emarsys-sdk:compileKotlinJs \
			 	-x :composeApp:build \
			 	-x :composeApp:jsPackageJson \
			 	-x :composeApp:jsTestPackageJson \
			 	-x :composeApp:jsPublicPackageJson \
			 	-x :composeApp:jsTestPublicPackageJson \
			 	-x :emarsys-sdk:jsTestPackageJson \
			 	-x :emarsys-sdk:jsPublicPackageJson \
			 	-x :emarsys-sdk:jsTestPublicPackageJson \
			 	-x :emarsys-sdk:jsPackageJson \
			 	-x :packageJsonUmbrella \
			 	-x :rootPackageJson \
			 	-x :kotlinNodeJsSetup \
			 	-x :kotlinNpmCachesSetup \
			 	-x :kotlinStoreYarnLock \
			 	-x :kotlinRestoreYarnLock \
			 	-x :kotlinYarnSetup \
			 	-x :kotlinNpmInstall \

clean: check-env ## clean all build artifacts
	@./gradlew clean

create-apks: check-env ## create apks for testing
	@./gradlew assembleAndroidTest -x :composeApp:test

test: check-env test-android test-web test-jvm ## run common tests on all platforms (jvm,web)
	@./gradlew :emarsys-sdk:allTests -x :composeApp:test

test-web: check-env ## run common tests on web
	@./gradlew :emarsys-sdk:jsBrowserTest \
 			-x :composeApp:test \
		 	-x :composeApp:jsPackageJson \
		 	-x :composeApp:jsTestPackageJson \
		 	-x :composeApp:jsPublicPackageJson \
		 	-x :composeApp:jsTestPublicPackageJson \
		 	-x :kotlinStoreYarnLock \
 			-x :kotlinUpgradeYarnLock

test-jvm: check-env ## run common tests on jvm
	@./gradlew :emarsys-sdk:test -x :composeApp:test

test-android: check-env test-fcm test-hms ## run Android Instrumented tests
	@./gradlew :emarsys-sdk:connectedAndroidTest -x :composeApp:test

test-fcm: check-env ## run FCM module tests
	@./gradlew :android-emarsys-sdk-fcm:connectedAndroidTest

test-hms: check-env ## run Huawei module tests
	@./gradlew :android-emarsys-sdk-hms:connectedAndroidTest

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

link-ios: ## runs gradle clean and then links iOS frameworks for supported arch types
	@./gradlew :emarsys-sdk:linkReleaseFrameworkIosX64 :emarsys-sdk:linkReleaseFrameworkIosArm64 :emarsys-sdk:linkReleaseFrameworkIosSimulatorArm64

remove-temp-lipo:
	@rm -rf lipo

remove-old-output:
	@rm -rf ./EmarsysSDK.xcframework

temp-lipo-dir:
	@mkdir -p lipo

lipo-merge: ## merges conflicting archs for simulator
	@lipo -create \
        emarsys-sdk/build/bin/iosX64/releaseFramework/EmarsysSDK.framework/EmarsysSDK \
        emarsys-sdk/build/bin/iosSimulatorArm64/releaseFramework/EmarsysSDK.framework/EmarsysSDK \
        -output lipo/EmarsysSDK

copy-lipo-output: ## copies the lipo output file and replaces the existing one in destination
	@\cp ./lipo/EmarsysSDK emarsys-sdk/build/bin/iosSimulatorArm64/releaseFramework/EmarsysSDK.framework/EmarsysSDK

ios-framework: link-ios remove-temp-lipo remove-old-output temp-lipo-dir lipo-merge copy-lipo-output
	@xcodebuild -create-xcframework \
         -framework emarsys-sdk/build/bin/iosArm64/releaseFramework/EmarsysSDK.framework \
          -framework emarsys-sdk/build/bin/iosSimulatorArm64/releaseFramework/EmarsysSDK.framework \
          -output EmarsysSDK.xcframework

lint: check-env ## run Android Instrumented tests
	@./gradlew :emarsys-sdk:lint -x :composeApp:lint