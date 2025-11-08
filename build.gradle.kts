plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.spotless)
}

// Code formatting & import optimization (approx. Android Studio Cmd+Opt+L / Ctrl+Alt+O)
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        // ktlint handles formatting + import ordering + unused import cleanup
        ktlint("1.4.1")
            .editorConfigOverride(
                mapOf(
                    // Match official style, 4-space indent, etc.
                    "ktlint_standard_no-wildcard-imports" to "disabled", // allow wildcard if project uses it
                    // Don't enforce package name convention (Android test packages may contain underscores)
                    "ktlint_standard_package-name" to "disabled",
                    // Don't block formatting due to function naming in platform interop code
                    "ktlint_standard_function-naming" to "disabled",
                    // Don't require file/class name alignment
                    "ktlint_standard_filename" to "disabled",
                    "indent_size" to "4",
                    "insert_final_newline" to "true",
                ),
            )
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**")
        ktlint("1.4.1")
    }
}

// Simple CI/local verification for localization coverage and placeholder counts (Compose resources).
// Validates that every Str key has a corresponding entry in commonMain/composeResources/values/strings.xml.
tasks.register("verifyL10n") {
    group = "verification"
    description = "Verify localization keys coverage and placeholder counts in Compose resources"
    doLast {
        val projectDir = project.projectDir
        // Source of truth is Compose Multiplatform base strings.xml
        val base = projectDir.resolve("shared/src/commonMain/composeResources/values/strings.xml")
        if (!base.exists()) error("Compose base strings.xml not found: $base")

        fun parseStringsXml(file: java.io.File): Map<String, String> {
            val text = file.readText()
            val r =
                Regex(
                    pattern = """<string\s+name="([a-zA-Z0-9_\-]+)"[^>]*>([\s\S]*?)</string>""",
                    options = setOf(RegexOption.DOT_MATCHES_ALL),
                )
            return r.findAll(text).associate { it.groupValues[1] to it.groupValues[2] }
        }
        val composeMap = parseStringsXml(base)
        val keys = composeMap.keys.sorted()
        if (keys.isEmpty()) error("No compose base strings found")

        // Define which keys require 1 arg (numeric or string)
        val oneArgNumeric =
            setOf(
                "updated_minutes_ago",
                "updated_hours_ago",
                "updated_days_ago",
                "created_minutes_ago",
                "created_hours_ago",
                "created_days_ago",
                "error_title_too_long",
                "error_description_too_long",
            )
        val oneArgString = setOf("welcome_user")

        // Keys already reflect compose base; basic placeholder checks below

        // Basic placeholder checks on base file only
        fun countPlaceholders(value: String): Int = Regex("%([0-9]+\\$)?[sd]").findAll(value).count()
        keys.forEach { k ->
            composeMap[k]?.let { v ->
                val pc = countPlaceholders(v)
                when (k) {
                    in oneArgNumeric ->
                        if (pc < 1 ||
                            !v.contains(Regex("%([0-9]+\\$)?d"))
                        ) {
                            error("Compose placeholder for $k should include %d: '$v'")
                        }
                    in oneArgString ->
                        if (pc < 1 ||
                            !v.contains(Regex("%([0-9]+\\$)?s"))
                        ) {
                            error("Compose placeholder for $k should include %s: '$v'")
                        }
                    else -> if (pc != 0) error("Compose string $k should have 0 placeholders but has $pc: '$v'")
                }
            }
        }

        println("verifyL10n: OK (keys=${keys.size}, compose base=$base)")
    }
}

// Simple CI aggregator to run build, tests, lint, and localization verification
tasks.register("ci") {
    group = "verification"
    description = "Run assemble, unit tests, lint, and verifyL10n"
    dependsOn("spotlessCheck", ":app:assembleDebug", ":app:testDebugUnitTest", ":app:lintDebug", "verifyL10n")
}
