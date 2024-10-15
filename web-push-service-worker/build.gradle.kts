plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
}

kotlin {
    js {
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
        }
        jsMain {
            dependencies {
                implementation(libs.kotlin.wrapper.browser)
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
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
