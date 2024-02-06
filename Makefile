.PHONY: build clean test test-web test-android test-jvm check-env help
.DEFAULT_GOAL := help

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

build: check-env ## compile and build all modules for all platforms
	@./gradlew :emarsys-sdk:build -x :emarsys-sdk:test -x :emarsys-sdk:testDebugUnitTest -x :emarsys-sdk:testReleaseUnitTest -x :emarsys-sdk:jsBrowserTest -x composeApp:build -x :emarsys-sdk:lint

clean: check-env ## clean all build artifacts
	@./gradlew clean

test: check-env ## run common tests on all platforms (jvm,web)
	@./gradlew :emarsys-sdk:allTests -x :composeApp:test

test-web: check-env ## run common tests on web
	@./gradlew :emarsys-sdk:jsBrowserTest -x :composeApp:test

test-jvm: check-env ## run common tests on jvm
	@./gradlew :emarsys-sdk:test -x :composeApp:test

test-android: check-env ## run Android Instrumented tests
	@./gradlew :emarsys-sdk:connectedAndroidTest -x :composeApp:test