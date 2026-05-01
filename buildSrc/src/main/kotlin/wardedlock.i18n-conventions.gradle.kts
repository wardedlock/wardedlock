import java.nio.charset.StandardCharsets

plugins {
    java
}

/**
 * i18n properties ASCII escape conventions.
 * Adds two tasks:
 *   nativeToAscii      : convert all non-English i18n .properties files to pure ASCII
 *                        using \uXXXX escapes for any character > 127.
 *   nativeToAsciiCheck : verify all such files are already pure ASCII; fail otherwise.
 */

fun collectI18nFiles(project: Project): List<java.io.File> {
    val dir = project.file("src/main/resources/i18n")
    if (!dir.exists()) return emptyList()
    return dir.listFiles { f ->
        f.isFile && f.name.endsWith(".properties") && !f.name.endsWith("_en.properties")
    }?.toList() ?: emptyList()
}

fun toAsciiEscaped(input: String): String {
    val sb = StringBuilder(input.length)
    for (c in input) {
        if (c.code < 0x80) {
            sb.append(c)
        } else {
            sb.append("\\u").append("%04x".format(c.code))
        }
    }
    return sb.toString()
}

tasks.register("nativeToAscii") {
    group = "i18n"
    description = "Convert non-English i18n .properties files to pure ASCII (\\uXXXX escapes)."
    doLast {
        val files = collectI18nFiles(project)
        if (files.isEmpty()) {
            logger.lifecycle("[nativeToAscii] No i18n files found in ${project.name}.")
            return@doLast
        }
        var changed = 0
        files.forEach { file ->
            val original = file.readText(StandardCharsets.UTF_8)
            val converted = toAsciiEscaped(original)
            if (converted != original) {
                file.writeText(converted, StandardCharsets.US_ASCII)
                changed++
                logger.lifecycle("[nativeToAscii] Converted: ${file.relativeTo(project.rootDir)}")
            }
        }
        logger.lifecycle("[nativeToAscii] Done. $changed/${files.size} file(s) updated in ${project.name}.")
    }
}

tasks.register("nativeToAsciiCheck") {
    group = "verification"
    description = "Verify non-English i18n .properties files are pure ASCII. Fails if any non-ASCII char is found."
    doLast {
        val files = collectI18nFiles(project)
        val violations = mutableListOf<String>()
        files.forEach { file ->
            val text = file.readText(StandardCharsets.UTF_8)
            text.forEachIndexed { idx, c ->
                if (c.code >= 0x80) {
                    violations.add("${file.relativeTo(project.rootDir)} at offset $idx: U+${"%04X".format(c.code)} ('$c')")
                    return@forEach // one violation per file is enough
                }
            }
        }
        if (violations.isNotEmpty()) {
            violations.forEach { logger.error("[nativeToAsciiCheck] $it") }
            throw GradleException(
                "Found ${violations.size} i18n file(s) with non-ASCII characters. " +
                "Run npm run i18n:fix (or ./gradlew nativeToAscii) and commit the result."
            )
        }
        logger.lifecycle("[nativeToAsciiCheck] OK. ${files.size} file(s) checked in ${project.name}.")
    }
}

// Wire the check into the standard verification lifecycle
tasks.named("check") {
    dependsOn("nativeToAsciiCheck")
}
