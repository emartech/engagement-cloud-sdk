@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import co.touchlab.skie.configuration.DefaultArgumentInterop
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.github.gmazzo.buildconfig.BuildConfigTask
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Base64

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
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
    }

    // Dynamic JS variant selection: -Pjs.variant=html or -Pjs.variant=canvas (default: canvas)
    val jsVariant = project.findProperty("js.variant")?.toString() ?: "html"
    println("Building JS variant: $jsVariant")

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "emarsys-sdk-$jsVariant.js"
                cssSupport {
                    enabled.set(false)
                }
            }
            webpackTask {
                mode = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.PRODUCTION
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()

        compilerOptions {
            moduleKind.set(org.jetbrains.kotlin.gradle.dsl.JsModuleKind.MODULE_ES)
            sourceMap.set(false)
            freeCompilerArgs.addAll(listOf(
                "-Xir-dce",
                "-Xir-minimized-member-names=true",
                "-Xir-per-module-output-name=false",
                "-Xpartial-linkage=enable"))
        }
        generateTypeScriptDefinitions()
    }

    applyDefaultHierarchyTemplate()

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
                implementation(project.dependencies.platform(libs.cryptography))
                implementation(libs.cryptography.core)
                implementation(compose.runtime)
                implementation(libs.androidx.paging.common)
                implementation(libs.androidx.paging.compose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.koin.test)
                implementation(libs.ktor.client.mock)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.table)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val commonComposeMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.compose.foundation)
                implementation(libs.compose.material)
                implementation(libs.compose.ui)
                implementation(libs.compose.resources)
                implementation(libs.compose.plugin.ui.tooling.preview)
                implementation(libs.compose.material.icons)

                implementation(libs.compose.adaptive)
                implementation(libs.compose.adaptive.navigation)
                implementation(libs.compose.ui.backhandler)
            }
        }
        val androidMain by getting {
            dependsOn(commonComposeMain)
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
                implementation(libs.androidx.paging.runtime)
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
                implementation(libs.kotest.assertions.table)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.okio.fakefilesystem)
            }
        }
        val iosMain by getting {
            dependsOn(commonComposeMain)
            dependencies {
                implementation(libs.ktor.client.apple)
                implementation(libs.cryptography.provider.apple)
                implementation(libs.sqlDelight.native)
            }
        }
        val iosTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.table)
                implementation(libs.kotlinx.coroutines.test)
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
                implementation(libs.kotest.assertions.table)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jsHtml by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.compose.html.core)
                implementation(libs.compose.html.svg)
            }
        }

        when (jsVariant) {
            "html" -> jsMain.dependsOn(jsHtml)
            "canvas" -> jsMain.dependsOn(commonComposeMain)
            else -> error("Invalid js.variant: $jsVariant. Use 'html' or 'canvas'")
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

tasks.register<Exec>("sdkLoaderTest") {
    group = "verification"
    description = "Run Node.js tests using the built-in node:test runner"

    workingDir = projectDir

    commandLine(
        "npm",
        "test",
    )
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