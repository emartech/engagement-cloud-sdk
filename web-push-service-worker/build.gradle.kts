plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
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
        }
        jsMain {
            dependencies {
                implementation(libs.kotlin.wrapper.browser)
            }
        }
    }
}
