import co.touchlab.skie.configuration.DefaultArgumentInterop
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.github.gmazzo.buildconfig.BuildConfigTask
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.skie)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kmmbridge)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

group = "com.emarsys"
version = "4.0.0"

kotlin {
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
    }
    js(IR) {
        binaries.executable()
        generateTypeScriptDefinitions()
        browser {
            commonWebpackConfig {
                outputFileName = "emarsys-sdk.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
            binaries.executable()
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "EmarsysSDK"
            isStatic = false
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.okio)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.serialization)
                implementation(project.dependencies.platform(libs.cryptography))
                implementation(libs.cryptography.core)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.koin.test)
                implementation(libs.ktor.client.mock)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.android)
                implementation(libs.androidx.core.ktx)
                implementation(libs.cryptography.provider.jdk)
                implementation(libs.startup.runtime)
                implementation(libs.androidx.lifecycle.common)
                implementation(libs.androidx.lifecycle.process)
                implementation(libs.androidx.appcompat)
                implementation(libs.sqlDelight.android)
                implementation(libs.google.play.services.location)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.ui.tooling)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mockk.android)
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.koin.test)
//                implementation(libs.koin.android.startup)
                implementation(libs.mockk.android)
                implementation(libs.androidx.runner)
                implementation(libs.androidx.test.junit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.okio.fakefilesystem)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.apple)
                implementation(libs.cryptography.provider.apple)
                implementation(libs.sqlDelight.native)
            }
        }
        iosTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.kotlin.wrapper.browser)
                implementation(libs.cryptography.provider.webcrypto)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

android {
    namespace = "com.emarsys"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "com.emarsys.SdkTestInstrumentationRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

buildConfig {
    packageName("com.emarsys.core.device")
    buildConfigField("String", "VERSION_NAME", "\"$version\"")
}

afterEvaluate {
    tasks.withType<AndroidLintAnalysisTask>()
        .configureEach { // https://github.com/gmazzo/gradle-buildconfig-plugin/issues/67
            mustRunAfter(tasks.withType<BuildConfigTask>())
        }
}

sqldelight {
    databases {
        create("EmarsysDB") {
            packageName.set("com.emarsys.sqldelight")
        }
    }
}

tasks.withType<app.cash.sqldelight.gradle.SqlDelightTask>().configureEach {
    doLast("workaround https://github.com/sqldelight/sqldelight/issues/1333") {
        outputDirectory.get().asFile.walk()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.writeText(
                    file.readText()
                        .replace("public data class Events", "internal data class Events")
                        .replace("public interface EmarsysDB", "internal interface EmarsysDB")
                        .replace("public class EventsQueries", "internal class EventsQueries")
                )
            }
    }
}

kmmbridge {
    val spmBuildType = System.getenv("SPM_BUILD") ?: "dev"
    when (spmBuildType) {
        "dev" -> {
            println("Building for local SPM development")
            spm()
        }

        "release" -> {
            println("Building for release")
            spm(
                spmDirectory = "./iosReleaseSpm",
                useCustomPackageFile = true,
                perModuleVariablesBlock = true
            )
        }

        else -> {
            println("Unknown SPM build type: $spmBuildType. Defaulting to local SPM development.")
        }
    }
}

skie {
    features {
        group {
            DefaultArgumentInterop.Enabled(true)
        }
    }

    build {
        enableSwiftLibraryEvolution = true
        produceDistributableFramework()
    }

    analytics {
        disableUpload.set(true)
    }
}
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true,
        )
    )

    coordinates(group.toString(), "emarsys-sdk", version.toString())

    pom {
        name = "Emarsys SDK"
        description = "Emarsys SDK"
        inceptionYear = "2025"
        url = "https://github.com/emartech/kmp-emarsys-sdk/"
        licenses {
            license {
                name = "Mozilla Public License 2.0"
                url = "https://github.com/emartech/kmp-emarsys-sdk/blob/main/LICENSE"
                distribution = "https://github.com/emartech/kmp-emarsys-sdk/blob/main/LICENSE"
            }
        }
        developers {
            developer {
                id = "emarsys"
                name = "Emarsys"
                url = "https://emarsys.com"
            }
        }
        scm {
            url = "https://github.com/emartech/kmp-emarsys-sdk"
            connection = "scm:git:https://github.com/emartech/kmp-emarsys-sdk.git"
            developerConnection = "scm:git:https://github.com/emartech/kmp-emarsys-sdk.git"
        }
    }
}

tasks {
    register("base64EnvToFile") {
        doLast {
            val propertyName = project.property("propertyName") as String?
                ?: throw IllegalArgumentException("Property 'propertyName' is not provided.")
            val file = project.property("file") as String?
                ?: throw IllegalArgumentException("Property 'file' is not provided.")
            val base64String = env.fetch(propertyName)
            val decoder = Base64.getDecoder()
            val decodedBytes = decoder.decode(base64String)

            file(file).apply {
                writeBytes(decodedBytes)
            }
        }
    }
}