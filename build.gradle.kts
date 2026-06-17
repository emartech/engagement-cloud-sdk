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
    alias(libs.plugins.detekt) // applied at root so detektProjectBaseline can resolve detekt classpaths
    alias(libs.plugins.dotenv)
}

val sdkVersion = System.getenv("VERSION_OVERRIDE") ?: "4.0.0-LOCAL"

allprojects {
	extra["SDK_VERSION"] = sdkVersion
}

// Make the version catalog reachable from inside the subprojects {} block below.
// Declared up-top so its first use inside `subprojects {}` is preceded by its
// definition in the source order.
val Project.libs: org.gradle.accessors.dm.LibrariesForLibs
    get() = the()

// Root-level detekt classpath — needed by the `detektProjectBaseline` task
// defined at the bottom of this file. The same set of plugins is *also* added
// to every subproject's `detektPlugins` configuration in the `subprojects {}`
// block below — both copies are required (root for the project-wide baseline,
// subprojects for per-source-set tasks). Keep them in sync if you bump versions.
dependencies {
    detektPlugins(libs.detekt.rules.libraries)
    detektPlugins(libs.detekt.rules.ktlint.wrapper)
    detektPlugins(libs.detekt.rules.ruleauthors)
    detektPlugins(libs.detekt.rules.compose)
}

// ---------------------------------------------------------------------------
// Detekt static analysis (SPEC.md)
//
// Applied uniformly to every Kotlin-bearing subproject from this single block
// (per SPEC §6 decision: subprojects {} block, not buildSrc convention plugin).
//
// IMPORTANT — Kotlin version alignment:
//   detekt 2.0.0-alpha.3 was built against Kotlin 2.3.21; the project pins
//   Kotlin 2.3.10. Detekt's classpath needs its own Kotlin (2.3.21). Do NOT
//   introduce `configurations.all { resolutionStrategy.eachDependency { ... } }`
//   for `org.jetbrains.kotlin` without excluding the `detekt` configuration —
//   doing so will break detekt at runtime. See SPEC §10 V-6.
// ---------------------------------------------------------------------------
subprojects {
    plugins.apply("dev.detekt")

    // All detekt configuration runs inside `pluginManager.withPlugin(...)` so
    // it only fires after the plugin is materialized. Two reasons:
    //   1) Defensive: if a future subproject is added that's Kotlin-less and
    //      we change the apply strategy to gate on a Kotlin plugin, the
    //      configure / dependencies / task-wiring blocks will simply no-op
    //      instead of failing at configuration time.
    //   2) Safer ordering: ensures the DetektExtension and the per-source-set
    //      Detekt tasks exist before we touch them.
    pluginManager.withPlugin("dev.detekt") {
        extensions.configure<dev.detekt.gradle.extensions.DetektExtension>("detekt") {
            toolVersion = rootProject.libs.versions.detekt.get()
            config.setFrom(rootProject.files("config/detekt/detekt.yml"))
            baseline.set(rootProject.file("config/detekt/baseline.xml"))
            buildUponDefaultConfig = false
            parallel = true
            // Default failOnSeverity = Error — fail-fast on any new violation
            // outside the baseline (SPEC US-2).
            basePath.set(rootProject.projectDir)
        }

        dependencies {
            add("detektPlugins", rootProject.libs.detekt.rules.libraries)
            add("detektPlugins", rootProject.libs.detekt.rules.ktlint.wrapper)
            add("detektPlugins", rootProject.libs.detekt.rules.ruleauthors)
            add("detektPlugins", rootProject.libs.detekt.rules.compose)
        }

        tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
            reports {
                html.required.set(true)
                sarif.required.set(true)
                checkstyle.required.set(true) // detekt 2.0 XML format
                markdown.required.set(true)
            }
            // KMP source sets register generated dirs (sqldelight, KSP, buildconfig)
            // as Kotlin source roots. SourceTask.exclude() patterns are evaluated
            // relative to source roots, so glob `**/build/**` doesn't match a path
            // whose source root IS `.../build/generated/...`. We use a Spec on the
            // FileTreeElement to filter by absolute path instead — this runs at
            // execution time per-file, sees the final source list, and is
            // configuration-cache-safe (no Task.project capture). SPEC §7 + AC-3, D-4.
            //
            // Path normalization: `File.absolutePath` returns native separators
            // (`\` on Windows, `/` elsewhere). We normalize to `/` so the
            // substring match works on every host the project supports.
            exclude { element ->
                val path = element.file.absolutePath.replace(java.io.File.separatorChar, '/')
                "/build/generated/" in path || "/build/sources/" in path
            }
        }

        // Wire the per-source-set detekt tasks into each subproject's aggregate
        // `detekt` task. Detekt 2.0 registers a `detekt` task that, by default,
        // analyses only the JVM source set — on KMP modules this is empty, so
        // `./gradlew detekt` would silently skip commonMain/iosMain/jsMain etc.
        // (SPEC US-4 + §9 explicitly require KMP source sets to be analyzed.)
        //
        // We use `tasks.withType` lazily — it returns a TaskCollection that
        // configures dependencies without realizing tasks at configuration time.
        //
        // EXCLUSION 1 (Android type resolution): detekt 2.0's Android TR tasks
        //   (`detektMain`, `detektDebug`, `detektRelease`, `detektTest`,
        //    `detektMainAndroid`, `detektHostTestAndroid`, `detektDeviceTestAndroid`)
        // transitively depend on the full Android compilation graph, which
        // requires `google-services.json` to be materialized at build time.
        //
        // In v1 this matters because we ship without the type-resolution-only
        // `detekt-rules-libraries` rule set (deferred to v2 — see SPEC §10 D-3
        // and §12 follow-up): the noJdk baseline can't absorb TR findings,
        // and detekt 2.0's per-task baseline writing doesn't merge into our
        // single baseline.xml. Until v2 redesigns the baseline task, the TR
        // detekt tasks have nothing useful to add (the AST-only rules are
        // covered by `*SourceSet` tasks already), so we exclude them from the
        // aggregate. The TR variants stay registered by detekt — they're just
        // not invoked from `:detekt`.
        //
        // EXCLUSION 2 (iOS on Linux, AC-7): the iOS source-set detekt tasks
        // (`detektIosMainSourceSet`, `detektIosArm64MainSourceSet`, …)
        // transitively depend on `compileKotlinIos*`, which fails on Linux
        // because Compose Multiplatform iOS artifacts aren't resolvable from
        // a Linux host (Konan / Compose iOS targets need macOS). The iOS
        // targets themselves are registered unconditionally by the SDK's
        // build.gradle.kts (the project relies on `isMac` only to gate the
        // *binary framework* output, not the source-sets), so detekt sees
        // them on Linux too. We strip iOS source-set tasks from the
        // aggregate on non-Mac hosts; iOS coverage is provided by the
        // reviewer's macOS pass (SPEC AC-7b) and any future macOS CI job.
        //
        // Coverage note: pure-Android modules (`androidApp`,
        // `engagement-cloud-sdk-android-fcm`, `engagement-cloud-sdk-android-hms`)
        // have no `*SourceSet` tasks — their detekt tasks are
        // `detektMain`/`detektDebug`/`detektRelease`/`detektTest`, all type-
        // resolution. The aggregate `:detekt` on those modules is therefore
        // a clean no-op locally; CI invokes `detektMain` explicitly. KMP
        // modules (`engagement-cloud-sdk`, `composeApp`,
        // `ios-notification-service`, `web-push-service-worker`) DO have
        // `*SourceSet` tasks that fold into the aggregate.
        //
        // Bump-detection: if a future detekt bump renames per-source-set
        // tasks (drops the `SourceSet` suffix), `:detekt` on KMP modules
        // silently regresses to NO-SOURCE. Defense: T6's iOS verification +
        // the regular `./gradlew :engagement-cloud-sdk:detekt --dry-run`
        // check during PR review catch the regression. We do NOT add a
        // runtime assertion here because pure-Android modules legitimately
        // match zero tasks, and distinguishing "expected zero" from
        // "unexpected zero" requires gating on AGP plugin presence which
        // adds more surface than the assertion saves.
        val isMac = System.getProperty("os.name").contains("Mac", ignoreCase = true)
        tasks.named("detekt").configure {
            dependsOn(
                tasks.withType<dev.detekt.gradle.Detekt>()
                    .matching { task ->
                        if (!task.name.endsWith("SourceSet")) return@matching false
                        // On non-Mac hosts, skip iOS source-set tasks. Ios prefixes:
                        // detektIosMainSourceSet, detektIosArm64MainSourceSet,
                        // detektIosX64MainSourceSet, detektIosSimulatorArm64MainSourceSet
                        // (and the matching *TestSourceSet variants).
                        if (!isMac && task.name.contains("Ios")) return@matching false
                        true
                    }
            )
        }
    }
}

// Single-baseline task: walks every source set across every subproject and
// writes ONE config/detekt/baseline.xml (SPEC US-3 — "A single baseline.xml").
// Detekt 2.0's per-task baselines would otherwise overwrite each other.
//
// Note on `noJdk = true`: baseline regeneration deliberately runs without
// type resolution so it's host-portable (no google-services.json needed).
// This means the baseline does NOT cover type-resolution-only findings such as
// `LibraryEntitiesShouldNotBePublic`. The first time you run the type-resolution
// tasks (`./gradlew detektMainAndroid` etc.) in CI after a fresh baseline regen,
// any new TR findings will be reported and fail the build — that's intended.
// Triage them: fix the code, suppress with rationale, or regen a baseline on a
// host with the secret materialized. (SPEC §10 D-3.)
tasks.register<dev.detekt.gradle.DetektCreateBaselineTask>("detektProjectBaseline") {
    description = "Generates a single project-wide detekt baseline at config/detekt/baseline.xml."
    group = "verification"
    parallel.set(true)
    buildUponDefaultConfig.set(false)
    ignoreFailures.set(true)
    noJdk.set(true) // See note above. Host-portable, but skips TR rules.
    setSource(files(rootDir))
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline.set(file("$rootDir/config/detekt/baseline.xml"))
    // Wire detekt's own classpath + the rule-set plugins from the root project's
    // detektPlugins config. Use `named(...)` (Provider-based, lazy) instead of
    // `getByName(...)` (eager) — the latter resolves the Configuration at
    // configuration time, which is a Gradle 10 incompatibility (and triggers the
    // "Configuration 'detekt' was resolved during configuration time" warning).
    detektClasspath.from(rootProject.configurations.named("detekt"))
    pluginClasspath.from(rootProject.configurations.named("detektPlugins"))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")          // covers build/generated, build/sources, etc.
    exclude("**/generated/**")      // KSP/sqldelight outputs that some plugins write outside `build/`
    exclude("**/node_modules/**")   // defensive: web-push-service-worker lives next to package-lock.json
}
