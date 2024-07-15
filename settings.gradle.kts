pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://developer.huawei.com/repo/")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.huawei") {
                if (requested.id.id == "com.huawei.agconnect") {
                    useModule("com.huawei.agconnect:agcp:${requested.version}")
                }
            }
        }
    }
}

dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://developer.huawei.com/repo/")
    }
}
rootProject.name = "EmarsysSDK"
include(
    ":composeApp",
    ":emarsys-sdk",
    ":android-emarsys-sdk-hms",
    ":android-emarsys-sdk-fcm",
    ":ios-notification-service"
)
