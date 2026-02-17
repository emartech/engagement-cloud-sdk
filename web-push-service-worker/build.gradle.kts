plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
}

kotlin {
    js(IR) {
        browser {
            useCommonJs()
            commonWebpackConfig {
                outputFileName = "ec-service-worker.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":engagement-cloud-sdk"))

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotlinx.coroutines.test)
            // Exclude Compose dependencies that come transitively from :engagement-cloud-sdk
            implementation(project(":engagement-cloud-sdk"))

        }
        jsMain {
            dependencies {
                implementation(libs.kotlin.wrapper.browser)
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

tasks.register<Copy>("copyServiceWorkerToEngagementCloudSDKResources") {
    println("copying service worker to engagement-cloud-sdk resources")
    from(
        layout.buildDirectory.file("dist/js/productionExecutable/ec-service-worker.js"),
        layout.buildDirectory.file("dist/js/productionExecutable/ec-service-worker.js.map")
    )
    into(project(":engagement-cloud-sdk").layout.projectDirectory.dir("src/jsMain/resources"))
}

tasks.findByName("jsBrowserDistribution")?.finalizedBy("copyServiceWorkerToEngagementCloudSDKResources")

// Exclude Compose dependencies from JS test classpath to avoid skiko.mjs errors
configurations {
    val jsTestRuntimeClasspath by getting
    jsTestRuntimeClasspath.exclude(group = "org.jetbrains.compose.ui")
    jsTestRuntimeClasspath.exclude(group = "org.jetbrains.compose.foundation")
    jsTestRuntimeClasspath.exclude(group = "org.jetbrains.compose.material")
    jsTestRuntimeClasspath.exclude(group = "org.jetbrains.compose.runtime")
    jsTestRuntimeClasspath.exclude(group = "org.jetbrains.compose.components")
    jsTestRuntimeClasspath.exclude(group = "org.jetbrains.skiko")
}
