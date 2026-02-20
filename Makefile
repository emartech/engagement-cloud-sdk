.PHONY: build build-pipeline build-android build-ios build-ios-all-archtypes build-js-html build-web check-env clean create-apks help lint pipeline-android pipeline-js pipeline-ios prepare-release prepare-spm publish-android publish-ios-spm publish-npm release release-locally test test-android test-android-firebase test-ios test-sdk-loader test-web
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
	@echo "  build-android    - Build Android artifacts"
	@echo "  build-ios        - Build iOS artifacts"
	@echo "  build-js-html    - Build JS artifacts"
	@echo "  lint             - Run lint"
	@echo "  test             - Run all tests"
	@echo "  publish-maven    - Publish to GitHub Packages (Maven)"
	@echo "  publish-npm      - Publish to GitHub Packages (NPM)"
	@echo "  publish-ios-spm  - Publish iOS SPM"

clean-dist:
	@rm -rf dist

build: check-env
	@./gradlew :composeApp:yarnActualization && ./gradlew assemble

build-pipeline: check-env
	@./gradlew assemble

clean: check-env
	@./gradlew clean

create-apks: check-env
	@./gradlew :composeApp:assembleRelease

test: check-env test-android test-web test-sdk-loader test-ios

build-js-html: check-env
	@./gradlew :engagement-cloud-sdk:jsBrowserProductionWebpack \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserProductionWebpack

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
	@./gradlew :engagement-cloud-sdk:allTests -x :composeApp:test

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
			:engagement-cloud-sdk-android-fcm:publishMavenPublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk-android-hms:publishMavenPublicationToGitHubPackagesRepository \
			:ios-notification-service:publishKotlinMultiplatformPublicationToGitHubPackagesRepository) \
		-PENABLE_PUBLISHING=$(PUBLISH) \
		--no-daemon

pipeline-js: check-env
	@./gradlew \
		:engagement-cloud-sdk:jsBrowserProductionWebpack \
		:engagement-cloud-sdk:jsBrowserTest \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserProductionWebpack \
		-x :composeApp:jsBrowserTest \
		--no-daemon

pipeline-ios: check-env
	@./gradlew \
		:engagement-cloud-sdk:iosX64Test \
		$(if $(filter true,$(PUBLISH)), \
			:engagement-cloud-sdk:assembleEngagementCloudSDKReleaseXCFramework \
			:ios-notification-service:assembleEngagementCloudNotificationServiceReleaseXCFramework \
			:engagement-cloud-sdk:publishIosArm64PublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk:publishIosX64PublicationToGitHubPackagesRepository \
			:engagement-cloud-sdk:publishIosSimulatorArm64PublicationToGitHubPackagesRepository \
			:ios-notification-service:publishIosArm64PublicationToGitHubPackagesRepository \
			:ios-notification-service:publishIosX64PublicationToGitHubPackagesRepository \
			:ios-notification-service:publishIosSimulatorArm64PublicationToGitHubPackagesRepository \
			kmmBridgePublish) \
		-PENABLE_PUBLISHING=$(PUBLISH) \
		-PNATIVE_BUILD_TYPE='$(if $(filter true,$(PUBLISH)),RELEASE,DEBUG)' \
		-PGITHUB_ARTIFACT_RELEASE_ID=$(GITHUB_ARTIFACT_RELEASE_ID) \
		-PGITHUB_PUBLISH_TOKEN=$(GITHUB_TOKEN) \
		-PGITHUB_REPO=$(GITHUB_REPO) \
		--no-daemon