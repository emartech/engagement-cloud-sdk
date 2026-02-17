pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
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
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}
rootProject.name = "EngagementCloudSDK"
include(
    ":composeApp",
    ":engagement-cloud-sdk",
    ":engagement-cloud-sdk-android-hms",
    ":engagement-cloud-sdk-android-fcm",
    ":ios-notification-service",
    ":web-push-service-worker"
)
