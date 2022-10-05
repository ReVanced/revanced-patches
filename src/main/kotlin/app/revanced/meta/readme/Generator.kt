package app.revanced.meta.readme

import app.revanced.meta.Bundle
import app.revanced.patcher.data.Context
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.description
import app.revanced.patcher.extensions.PatchExtensions.patchName
import app.revanced.patcher.patch.Patch
import java.io.File

private const val TABLE_HEADER =
    "| \uD83D\uDC8A Patch | \uD83D\uDCDC Description | \uD83C\uDFF9 Target Version |\n" + "|:--------:|:--------------:|:-----------------:|"

private val TABLE_REGEX = Regex("\\{\\{\\s?table\\s?}}")

fun generateText(bundle: Bundle) {
    val output = StringBuilder()
    val packages = mutableMapOf<String, MutableList<Class<out Patch<Context>>>>()

    for (patch in bundle) {
        patch.compatiblePackages?.forEach { pkg ->
            if (!packages.contains(pkg.name)) packages[pkg.name] = mutableListOf()
            packages[pkg.name]!!.add(patch)
        }
    }

    for (pkg in packages) {
        output.appendLine("### \uD83D\uDCE6 `${pkg.key}`")
        output.appendLine("<details>\n")

        output.appendLine(TABLE_HEADER)
        pkg.value.forEach { output.appendLine("| `${it.patchName}` | ${it.description} | ${it.getLatestVersion() ?: "all"} |") }

        output.appendLine("</details>\n")
    }

    val readmeTemplate = Template(File("README-template.md").readText())
    readmeTemplate.replaceVariable(TABLE_REGEX, output.toString())

    val readme = File("README.md")
    readme.writeText(readmeTemplate.toString())
}
