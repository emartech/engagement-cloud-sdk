plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "ems-service-worker.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":emarsys-sdk"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotlinx.coroutines.test)
            // Exclude Compose dependencies that come transitively from :emarsys-sdk
            implementation(project(":emarsys-sdk")) {
                exclude(group = "org.jetbrains.compose.runtime")
                exclude(group = "org.jetbrains.compose.foundation")
                exclude(group = "org.jetbrains.compose.material3")
                exclude(group = "org.jetbrains.compose.ui")
                exclude(group = "org.jetbrains.compose.components")
            }
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

tasks.register<Copy>("copyServiceWorkerToEmarsysSDKResources") {
    println("copying service worker to emarsys-sdk resources")
    from(
        layout.buildDirectory.file("dist/js/productionExecutable/ems-service-worker.js"),
        layout.buildDirectory.file("dist/js/productionExecutable/ems-service-worker.js.map")
    )
    into(project(":emarsys-sdk").layout.projectDirectory.dir("src/jsMain/resources"))
}

tasks.findByName("jsBrowserDistribution")?.finalizedBy("copyServiceWorkerToEmarsysSDKResources")

// Exclude Compose dependencies from JS test classpath to avoid skiko.mjs errors
configurations {
    val jsTestRuntimeClasspath by getting {
        exclude(group = "org.jetbrains.compose.runtime")
        exclude(group = "org.jetbrains.compose.foundation")
        exclude(group = "org.jetbrains.compose.material3")
        exclude(group = "org.jetbrains.compose.ui")
        exclude(group = "org.jetbrains.compose.components")
    }
}
