package app.revanced.meta.readme

import app.revanced.patcher.data.Data
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.description
import app.revanced.patcher.extensions.PatchExtensions.patchName
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.util.patch.impl.JarPatchBundle
import java.io.File

object Generator {
    private const val TABLE_HEADER =
        "| \uD83D\uDC8A Patch | \uD83D\uDCDC Description | \uD83C\uDFF9 Target Version |\n" +
                "|:--------:|:--------------:|:-----------------:|"

    @JvmStatic
    fun main(args: Array<String>) {
        val buildDir = File("build/libs/")
        val buildJar =
            buildDir.listFiles()?.first { it.name.startsWith("revanced-patches-") && it.name.endsWith(".jar") }!!

        val bundle = JarPatchBundle(buildJar.absolutePath).loadPatches()

        val output = StringBuilder()

        val packages = mutableMapOf<String, MutableList<Class<out Patch<Data>>>>()

        bundle.map {
            val packageName = it.compatiblePackages?.first()?.name!!
            if (!packages.contains(packageName)) {
                packages[packageName] = mutableListOf()
            }

            packages[packageName]?.add(it)
        }

        for (pkg in packages) {
            output.appendLine("### \uD83D\uDCE6 `${pkg.key}`")
            output.appendLine("<details>\n")

            output.appendLine(TABLE_HEADER)
            pkg.value.forEach { output.appendLine("| `${it.patchName}` | ${it.description} | ${it.getLatestVersion() ?: "all"} |") }

            output.appendLine("</details>\n")
        }

        val readMeTemplateFile = File("README-template.md")
        val readmeTemplate = Template(readMeTemplateFile.readText())

        readmeTemplate.replaceVariable("table", output.toString())

        val readme = File("README.md")
        readme.writeText(readmeTemplate.toString())
    }
}
