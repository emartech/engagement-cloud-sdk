.PHONY: build build-pipeline build-android build-ios build-ios-all-archtypes build-js-html build-web check-env clean create-apks help lint prepare-release prepare-spm publish-android publish-ios-spm publish-npm release release-locally test test-android test-android-firebase test-ios test-sdk-loader test-web
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

test: check-env test-android test-web test-sdk-loader test-jvm test-ios

build-js-html: check-env
	@./gradlew :engagement-cloud-sdk:jsBrowserProductionWebpack \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserProductionWebpack

build-web: build-js-html

test-web: check-env
	@./gradlew :engagement-cloud-sdk:jsBrowserProductionLibraryTest \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserProductionLibraryTest

test-sdk-loader: check-env
	@./gradlew :engagement-cloud-sdk:jsBrowserProductionLibraryTest \
		-Pjs.variant=html \
		-x :composeApp:jsBrowserProductionLibraryTest \
		--tests com.sap.ec.mobileengage.WebSdkLoaderTest

test-jvm: check-env
	@./gradlew :engagement-cloud-sdk:jvmTest

build-android: check-env
	@./gradlew \
		:engagement-cloud-sdk:assembleAndroidRelease \
		:engagement-cloud-sdk-android-fcm:assembleRelease \
		:engagement-cloud-sdk-android-hms:assembleRelease

test-android: check-env
	@./gradlew \
		:engagement-cloud-sdk:testAndroidReleaseUnitTest \
		:engagement-cloud-sdk-android-fcm:testReleaseUnitTest \
		:engagement-cloud-sdk-android-hms:testReleaseUnitTest

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
		:engagement-cloud-sdk:lintRelease \
		:engagement-cloud-sdk-android-fcm:lintRelease \
		:engagement-cloud-sdk-android-hms:lintRelease

prepare-spm: check-env
	@./gradlew spmDevBuild && \
	cp -f "./iosReleaseSpm/Package.swift" "./Package.swift" && \
	echo "Swift Package is prepared. To use it as a local dependency add the project in Xcode at the Package Dependencies section"

publish-maven: check-env
	@./gradlew \
		-PENABLE_PUBLISHING=true \
		:engagement-cloud-sdk:publishAllPublicationsToGitHubPackagesRepository \
		:engagement-cloud-sdk-android-fcm:publishReleasePublicationToGitHubPackagesRepository \
		:engagement-cloud-sdk-android-hms:publishReleasePublicationToGitHubPackagesRepository

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
	@./gradlew assembleRelease && ./gradlew publishToMavenCentral

release-locally: check-env prepare-release
	@./gradlew assembleRelease && ./gradlew publishToMavenLocal