buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:${property("agpVersion")}")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.buildConfig) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.agconnect) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.dotenv)
}

val sdkVersion = System.getenv("VERSION_OVERRIDE") ?: "4.0.0-LOCAL"

allprojects {
	extra["SDK_VERSION"] = sdkVersion
}

val detektVersion = libs.versions.detekt.get()
val detektLibraries = libs.detekt.rules.libraries
val detektCompose = libs.detekt.rules.compose
val detektInvocation = gradle.startParameter.taskNames.any {
    it.substringAfterLast(':').startsWith("detekt")
}

// Analysis must not require sample-app credentials. Valid supplied JSON is still
// processed normally; ordinary build/lint invocations retain the plugin's default ERROR strategy.
subprojects {
    pluginManager.withPlugin("com.google.gms.google-services") {
        if (detektInvocation) {
            extensions.configure<com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig> {
                missingGoogleServicesStrategy =
                    com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy.IGNORE
            }
        }
    }
}

subprojects {
    pluginManager.apply("dev.detekt")

    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        toolVersion = detektVersion
        buildUponDefaultConfig = false
        allRules = false
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        ignoreFailures = false
        basePath.set(rootProject.layout.projectDirectory)
    }
}

val publishedLibraryProjects = setOf(
    ":engagement-cloud-sdk",
    ":engagement-cloud-sdk-android-fcm",
    ":engagement-cloud-sdk-android-hms",
    ":ios-notification-service",
)
val composeProjects = setOf(":androidApp", ":composeApp", ":engagement-cloud-sdk")

extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
    toolVersion = detektVersion
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = false
    allRules = false
    basePath.set(layout.projectDirectory)
}
dependencies {
    add("detektPlugins", detektLibraries)
    add("detektPlugins", detektCompose)
}

allprojects {
    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        val reportStem = "${project.name}-$name"
        baseline.set(rootProject.layout.projectDirectory.file("config/detekt/baseline/${project.name}/$name.xml"))
        if (providers.gradleProperty("detekt.inventory").orNull == "true") {
            baseline.set(rootProject.layout.buildDirectory.file("detekt/unbaselined.xml"))
            ignoreFailures.set(true)
        }
        autoCorrect.set(false)
        reports {
            html.required.set(true)
            html.outputLocation.set(rootProject.layout.buildDirectory.file("reports/detekt/$reportStem.html"))
            checkstyle.required.set(true)
            checkstyle.outputLocation.set(rootProject.layout.buildDirectory.file("reports/detekt/$reportStem.xml"))
            sarif.required.set(true)
            sarif.outputLocation.set(rootProject.layout.buildDirectory.file("reports/detekt/$reportStem.sarif"))
            markdown.required.set(false)
        }
    }

    tasks.withType<dev.detekt.gradle.DetektCreateBaselineTask>().configureEach {
        val analysisTaskName = name.replaceFirst("detektBaseline", "detekt")
        baseline.set(
            rootProject.layout.projectDirectory.file(
                "config/detekt/baseline/${project.name}/$analysisTaskName.xml",
            ),
        )
        autoCorrect.set(false)
    }

    dependencies {
        if (path in publishedLibraryProjects) {
            add("detektPlugins", detektLibraries)
        }
        if (path in composeProjects) {
            add("detektPlugins", detektCompose)
        }
    }
}

tasks.register<JavaExec>("validateDetektPolicy") {
    group = "verification"
    description = "Compares explicit policy with the pinned runtime rule providers."
    classpath = configurations.getByName("detekt") + configurations.getByName("detektPlugins")
    mainClass.set(file("scripts/detekt/VerifyDetektPolicy.java").absolutePath)
    args(file("config/detekt/detekt.yml").absolutePath)
    inputs.files("scripts/detekt/VerifyDetektPolicy.java", "config/detekt/detekt.yml")
}

val canonicalDetektTasks = mapOf(
    ":androidApp" to listOf("detektMain", "detektTest"),
    ":composeApp" to listOf(
        "detektMainAndroid", "detektCommonTestSourceSet", "detektIosMainSourceSet",
        "detektIosTestSourceSet", "detektJsMainSourceSet", "detektJsTestSourceSet",
    ),
    ":engagement-cloud-sdk" to listOf(
        "detektMainAndroid", "detektHostTestAndroid", "detektDeviceTestAndroid",
        "detektIosMainSourceSet", "detektIosTestSourceSet", "detektJsMainSourceSet",
        "detektJsTestSourceSet", "detektJsHtmlSourceSet",
    ),
    ":engagement-cloud-sdk-android-fcm" to listOf("detektMain", "detektTest"),
    ":engagement-cloud-sdk-android-hms" to listOf("detektMain", "detektTest"),
    ":ios-notification-service" to listOf(
        "detektCommonMainSourceSet", "detektCommonTestSourceSet",
        "detektIosMainSourceSet", "detektIosTestSourceSet",
    ),
    ":web-push-service-worker" to listOf(
        "detektCommonMainSourceSet", "detektCommonTestSourceSet",
        "detektJsMainSourceSet", "detektJsTestSourceSet",
    ),
)


allprojects {
    val buildScripts = files(file("build.gradle.kts"))
    if (this == rootProject) buildScripts.from(file("settings.gradle.kts"))
    tasks.register<dev.detekt.gradle.Detekt>("detektBuildScripts") {
        description = "Analyzes this project's Gradle Kotlin scripts without library/Compose API rules."
        setSource(buildScripts)
        pluginClasspath.setFrom(emptyList<File>())
    }
    tasks.register<dev.detekt.gradle.DetektCreateBaselineTask>("detektBaselineBuildScripts") {
        setSource(buildScripts)
        pluginClasspath.setFrom(emptyList<File>())
    }
}

val detektBaselineAll = tasks.register("detektBaselineAll") {
    group = "verification"
    description = "Generates baselines for the canonical KMP Detekt matrix."
}

val detektAll = tasks.register("detektAll") {
    group = "verification"
    description = "Runs the canonical complete KMP Detekt analysis (CI only)."
    dependsOn("validateDetektPolicy")
}

gradle.projectsEvaluated {
    canonicalDetektTasks.forEach { (projectPath, taskNames) ->
        val targetProject = project(projectPath)
        val hostTasks = if (projectPath == ":ios-notification-service") {
            listOf("detektMainJvm", "detektTestJvm").filter { targetProject.tasks.findByName(it) != null }
        } else emptyList()
        (taskNames + hostTasks).forEach { taskName ->
            detektAll.configure { dependsOn(targetProject.tasks.named(taskName)) }
            val baselineTaskName = taskName.replaceFirst("detekt", "detektBaseline")
            detektBaselineAll.configure { dependsOn(targetProject.tasks.named(baselineTaskName)) }
        }
    }
    allprojects {
        detektAll.configure { dependsOn(tasks.named("detektBuildScripts")) }
        detektBaselineAll.configure { dependsOn(tasks.named("detektBaselineBuildScripts")) }
        tasks.matching { it.name == "check" }.configureEach {
            setDependsOn(dependsOn.filterNot { dependency ->
                val dependencyName = when (dependency) {
                    is org.gradle.api.tasks.TaskProvider<*> -> dependency.name
                    is Task -> dependency.name
                    is String -> dependency.substringAfterLast(':')
                    else -> ""
                }
                dependencyName.startsWith("detekt")
            })
        }
    }
}
