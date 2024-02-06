.PHONY: build clean test test-web test-android test-jvm help
.DEFAULT_GOAL := help

ifeq (,$(wildcard .env))
$(error No .env file available, copy from https://secret.emarsys.net/cred/detail/18243/)
endif

help: ## Show this help
	@echo "Targets:"
	@fgrep -h "##" $(MAKEFILE_LIST) | grep ":" | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/\(.*\):.*##[ \t]*/    \1 ## /' | sort | column -t -s '##'
	@echo

build: ## compile and build all modules for all platforms
	@./gradlew :emarsys-sdk:build -x :emarsys-sdk:test -x :emarsys-sdk:testDebugUnitTest -x :emarsys-sdk:testReleaseUnitTest -x :emarsys-sdk:jsBrowserTest -x composeApp:build -x :emarsys-sdk:lint

clean: ## clean all build artifacts
	@./gradlew clean

test: ## run common tests on all platforms (jvm,web)
	@./gradlew :emarsys-sdk:allTests -x :composeApp:test

test-web: ## run common tests on web
	@./gradlew :emarsys-sdk:jsBrowserTest -x :composeApp:test

test-jvm: ## run common tests on jvm
	@./gradlew :emarsys-sdk:test -x :composeApp:test

test-android: ## run Android Instrumented tests
	@./gradlew :emarsys-sdk:connectedAndroidTest -x :composeApp:test