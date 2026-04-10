.PHONY: build build-pipeline build-android build-ios build-ios-all-archtypes build-js-html build-web check-env clean create-apks help lint pipeline-android pipeline-js pipeline-ios prepare-release prepare-spm prepare-local-spm publish-android publish-ios-spm publish-npm release release-locally stage-maven-central test test-android test-android-firebase test-ios test-sdk-loader test-web add-privacy-manifest-to-frameworks
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
help:
	@echo "Available targets:"
	@echo "  build-android               - Build Android artifacts"
	@echo "  build-ios                   - Build iOS artifacts"
	@echo "  build-js-html               - Build JS artifacts"
	@echo "  lint                        - Run lint"
	@echo "  test                        - Run all tests"
	@echo "  prepare-local-spm           - Build local iOS frameworks for iosApp testing (no Privacy Manifest)"
	@echo "  add-privacy-manifest-to-frameworks - Add Privacy Manifest to release XCFrameworks (run after building release frameworks)"
	@echo "  publish-maven               - Publish to GitHub Packages (Maven)"
	@echo "  publish-npm                 - Publish to GitHub Packages (NPM)"
	@echo "  publish-ios-spm             - Publish iOS SPM"

clean-dist:
	@rm -rf dist

build: check-env
	@./gradlew assemble

build-pipeline: check-env
	@./gradlew assemble

clean: check-env
	@./gradlew clean

create-apks: check-env
	@./gradlew :androidApp:assembleRelease

test: check-env test-android test-web test-sdk-loader test-ios

build-js-html: check-env
	@./gradlew :engagement-cloud-sdk:jsBrowserDistribution \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserDistribution

build-web: build-js-html

test-web: check-env
	@./gradlew :engagement-cloud-sdk:jsBrowserTest \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserTest

test-sdk-loader: check-env
	@./gradlew :engagement-cloud-sdk:jsBrowserTest \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserTest \
		--tests com.sap.ec.mobileengage.WebSdkLoaderTest

build-android: check-env
	@./gradlew \
		:engagement-cloud-sdk:assembleAndroidMain \
		:engagement-cloud-sdk-android-fcm:assembleRelease \
		:engagement-cloud-sdk-android-hms:assembleRelease

test-android: check-env
	@./gradlew \
		:engagement-cloud-sdk:testAndroidHostTest \
		:engagement-cloud-sdk:connectedAndroidDeviceTest \
		:engagement-cloud-sdk-android-fcm:testDebugUnitTest \
		:engagement-cloud-sdk-android-hms:testDebugUnitTest

build-ios-all-archtypes: check-env
	@./gradlew \
		:engagement-cloud-sdk:assembleEngagementCloudSDKReleaseXCFramework \
		:ios-notification-service:assembleEngagementCloudNotificationServiceReleaseXCFramework

build-ios: check-env
	@./gradlew :engagement-cloud-sdk:iosArm64Binaries

test-ios: check-env
	@./gradlew :engagement-cloud-sdk:iosX64Test

test-android-firebase: check-env
	@gcloud firebase test android run \
		--type instrumentation \
		--app engagement-cloud-sdk/build/outputs/apk/debug/engagement-cloud-sdk-debug.apk \
		--test engagement-cloud-sdk/build/outputs/apk/androidTest/debug/engagement-cloud-sdk-debug-androidTest.apk \
		--device model=Pixel3,version=30,locale=en,orientation=portrait

lint: check-env
	@./gradlew \
		:engagement-cloud-sdk-android-fcm:lintRelease \
		:engagement-cloud-sdk-android-hms:lintRelease

prepare-spm: check-env
	@./gradlew \
		-PENABLE_PUBLISHING=true \
		spmDevBuild && \
		cp -f "./iosReleaseSpm/Package.swift" "./Package.swift" && \
		echo "Swift Package is prepared. To use it as a local dependency add the project in Xcode at the Package Dependencies section"

prepare-local-spm: check-env
	@./gradlew \
		-PENABLE_PUBLISHING=true \
		spmDevBuild && \
		cp -f "./iosReleaseSpm/Package.swift" "./Package.swift" && \
		echo "Local Swift Package is prepared."

add-privacy-manifest-to-frameworks:
	@echo "Adding Privacy Manifest to release XCFrameworks..."
	@PRIVACY_MANIFEST="engagement-cloud-sdk/src/iosMain/resources/PrivacyInfo.xcprivacy"; \
	if [ ! -f "$$PRIVACY_MANIFEST" ]; then \
		echo "Error: Privacy Manifest not found at $$PRIVACY_MANIFEST"; \
		exit 1; \
	fi; \
	find engagement-cloud-sdk/build/XCFrameworks/release -name "EngagementCloudSDK.framework" -type d | while read framework; do \
		echo "  → $$framework"; \
		cp "$$PRIVACY_MANIFEST" "$$framework/"; \
	done; \
	echo "Privacy Manifest added to all framework variants"

publish-maven: check-env
	@./gradlew \
		-PENABLE_PUBLISHING=true \
		:engagement-cloud-sdk:publishAllPublicationsToGitHubPackagesRepository \
		:engagement-cloud-sdk-android-fcm:publishMavenPublicationToGitHubPackagesRepository \
		:engagement-cloud-sdk-android-hms:publishMavenPublicationToGitHubPackagesRepository

publish-maven-ios: check-env
	@./gradlew \
		-PENABLE_PUBLISHING=true \
		:engagement-cloud-sdk:publishIosArm64PublicationToGitHubPackagesRepository \
		:engagement-cloud-sdk:publishIosX64PublicationToGitHubPackagesRepository \
		:engagement-cloud-sdk:publishIosSimulatorArm64PublicationToGitHubPackagesRepository \
		:ios-notification-service:publishIosArm64PublicationToGitHubPackagesRepository \
		:ios-notification-service:publishIosX64PublicationToGitHubPackagesRepository \
		:ios-notification-service:publishIosSimulatorArm64PublicationToGitHubPackagesRepository \
		:ios-notification-service:publishKotlinMultiplatformPublicationToGitHubPackagesRepository

publish-npm: check-env
	@cd dist/npm && npm publish --registry https://npm.pkg.github.com

publish-ios-spm: check-env
	@./gradlew kmmBridgePublish \
		-PNATIVE_BUILD_TYPE='RELEASE' \
		-PGITHUB_ARTIFACT_RELEASE_ID=$(GITHUB_ARTIFACT_RELEASE_ID) \
		-PGITHUB_PUBLISH_TOKEN=$(GITHUB_TOKEN) \
		-PGITHUB_REPO=$(GITHUB_REPO) \
		-PENABLE_PUBLISHING=true \
		--no-daemon

prepare-release: check-env
	@./gradlew base64EnvToFile -PpropertyName=SONATYPE_SIGNING_SECRET_KEY_RING_FILE_BASE64 -Pfile=./secring.asc.gpg

release: check-env prepare-release
	@./gradlew assembleRelease publishToMavenCentral -PPROMOTE_TO_MAVEN_CENTRAL=true

release-locally: check-env prepare-release
	@./gradlew assembleRelease publishToMavenLocal -PPROMOTE_TO_MAVEN_CENTRAL=true

stage-maven-central: check-env prepare-release
	@./gradlew publishToMavenCentral \
		-PPROMOTE_TO_MAVEN_CENTRAL=true \
		-PENABLE_PUBLISHING=true \
		--no-daemon

pipeline-android: check-env
	@./gradlew \
		:engagement-cloud-sdk:assembleAndroidMain \
		:engagement-cloud-sdk-android-fcm:assembleRelease \
		:engagement-cloud-sdk-android-hms:assembleRelease \
		:engagement-cloud-sdk-android-fcm:lintRelease \
		:engagement-cloud-sdk-android-hms:lintRelease \
		:engagement-cloud-sdk:testAndroidHostTest \
		:engagement-cloud-sdk-android-fcm:testDebugUnitTest \
		:engagement-cloud-sdk-android-hms:testDebugUnitTest \
		$(if $(filter true,$(PUBLISH)), \
			:engagement-cloud-sdk:publishKotlinMultiplatformPublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk:publishAndroidPublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk:publishJsPublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk:publishIosArm64PublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk:publishIosX64PublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk:publishIosSimulatorArm64PublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk-android-fcm:publishMavenPublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk-android-hms:publishMavenPublicationToGitHubPackagesRepository) \
		-Pjs.variant=canvas \
		-PENABLE_PUBLISHING=$(PUBLISH) \
		--no-daemon

pipeline-js: check-env
	@./gradlew \
		:web-push-service-worker:jsBrowserDistribution \
		:engagement-cloud-sdk:jsBrowserDistribution \
		:engagement-cloud-sdk:jsBrowserTest \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserDistribution \
		-x :composeApp:jsBrowserTest \
		--no-daemon

pipeline-ios: check-env
	@./gradlew \
		:engagement-cloud-sdk:iosX64Test \
		$(if $(filter true,$(PUBLISH)), \
			:engagement-cloud-sdk:assembleEngagementCloudSDKReleaseXCFramework \
			:ios-notification-service:assembleEngagementCloudNotificationServiceReleaseXCFramework) \
		-PNATIVE_BUILD_TYPE='$(if $(filter true,$(PUBLISH)),RELEASE,DEBUG)' \
		$(if $(filter true,$(PUBLISH)),-PENABLE_PUBLISHING=true) \
		--no-daemon