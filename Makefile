.PHONY: build build-pipeline check-env clean create-apks help lint test test-web test-android test-android-firebase test-jvm link-ios-sdk link-ios-notification-service remove-temp-lipo temp-dirs lipo-merge-ios-sdk lipo-merge-ios-notification-service copy-lipo-ios-emarsys-sdk-output copy-lipo-notification-service-output ios-sdk-framework ios-notification-service-framework
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
	@./gradlew assembleAndroidTest

test: check-env test-android test-web test-jvm test-ios ## run common tests on all platforms (jvm,web,android, ios)
	@./gradlew :emarsys-sdk:allTests -x :emarsys-sdk:testReleaseUnitTest -x :composeApp:test

build-web: check-env ## run tests on web
	@./gradlew jsBrowserProductionWebpack \
 			-x :composeApp:jsBrowserProductionWebpack

test-web: check-env ## run tests on web
	@./gradlew jsBrowserTest \
 			-x :composeApp:jsBrowserTest

build-android: check-env ##
	@./gradlew assembleRelease -x :composeApp:assembleRelease

test-android: check-env ## run Android tests for all modules
	@./gradlew connectedAndroidTest -x :composeApp:connectedAndroidTest

build-ios: check-env ## build iOS
	@./gradlew linkReleaseFrameworkIosArm64 \
	        -x :composeApp:linkReleaseFrameworkIosArm64

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

link-ios-sdk: ## links iOS frameworks for supported arch types
	@./gradlew :emarsys-sdk:linkReleaseFrameworkIosX64 :emarsys-sdk:linkReleaseFrameworkIosArm64 :emarsys-sdk:linkReleaseFrameworkIosSimulatorArm64

link-ios-notification-service: ## links iOS frameworks for supported arch types
	@./gradlew :ios-notification-service:linkReleaseFrameworkIosX64 :ios-notification-service:linkReleaseFrameworkIosArm64 :ios-notification-service:linkReleaseFrameworkIosSimulatorArm64

remove-temp-lipo: ## clean up lipo output directory
	@rm -rf lipo

temp-dirs: ## create temporary directories
	@mkdir -p lipo buildedFrameworks

lipo-merge-ios-sdk: ## merges conflicting emarsys-sdk archs for simulator
	@lipo -create \
        emarsys-sdk/build/bin/iosX64/releaseFramework/EmarsysSDK.framework/EmarsysSDK \
        emarsys-sdk/build/bin/iosSimulatorArm64/releaseFramework/EmarsysSDK.framework/EmarsysSDK \
        -output lipo/EmarsysSDK

lipo-merge-ios-notification-service: ## merges conflicting notification-service archs for simulator
	@lipo -create \
        ios-notification-service/build/bin/iosX64/releaseFramework/EmarsysNotificationService.framework/EmarsysNotificationService \
        ios-notification-service/build/bin/iosSimulatorArm64/releaseFramework/EmarsysNotificationService.framework/EmarsysNotificationService \
        -output lipo/EmarsysNotificationService

copy-lipo-ios-emarsys-sdk-output: ## copies the lipo output file for the sdk and replaces the existing one at the destination
	@\cp ./lipo/EmarsysSDK emarsys-sdk/build/bin/iosSimulatorArm64/releaseFramework/EmarsysSDK.framework/EmarsysSDK

copy-lipo-notification-service-output: ## copies the lipo output file for the notification-service and replaces the existing one at the destination
	@\cp ./lipo/EmarsysNotificationService  ios-notification-service/build/bin/iosSimulatorArm64/releaseFramework/EmarsysNotificationService.framework/EmarsysNotificationService

ios-sdk-framework: link-ios-sdk remove-temp-lipo temp-dirs lipo-merge-ios-sdk copy-lipo-ios-emarsys-sdk-output
	@rm -rf buildedFrameworks/EmarsysSDK.xcframework && \
	xcodebuild -create-xcframework \
         -framework emarsys-sdk/build/bin/iosArm64/releaseFramework/EmarsysSDK.framework \
          -framework emarsys-sdk/build/bin/iosSimulatorArm64/releaseFramework/EmarsysSDK.framework \
          -output buildedFrameworks/EmarsysSDK.xcframework

ios-notification-service-framework: link-ios-notification-service remove-temp-lipo temp-dirs lipo-merge-ios-notification-service copy-lipo-notification-service-output
	@rm -rf buildedFrameworks/EmarsysNotificationService.xcframework && \
	xcodebuild -create-xcframework \
         -framework ios-notification-service/build/bin/iosArm64/releaseFramework/EmarsysNotificationService.framework \
          -framework ios-notification-service/build/bin/iosSimulatorArm64/releaseFramework/EmarsysNotificationService.framework \
          -output buildedFrameworks/EmarsysNotificationService.xcframework

ios-frameworks: ios-sdk-framework ios-notification-service-framework

lint: check-env ## run Android Instrumented tests
	@./gradlew :emarsys-sdk:lint -x :composeApp:lint