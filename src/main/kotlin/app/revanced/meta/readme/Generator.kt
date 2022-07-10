package app.revanced.patches.meta.readme

import java.io.File
import kotlin.io.writeText
import kotlin.collections.first
import app.revanced.patcher.util.patch.implementation.JarPatchBundle
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.patchName
import app.revanced.patcher.extensions.PatchExtensions.description

class Generator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val buildDir = File("build/libs/")
            val buildJar = buildDir.listFiles().first { it.name.startsWith("revanced-patches-") && it.name.endsWith(".jar") }

            val bundle = JarPatchBundle(buildJar.absolutePath).loadPatches()

            val table = StringBuilder()

            for (patch in bundle) {
                val humanName =
                    patch.patchName.split('-').map { it.replaceFirstChar { it.uppercase() } }.joinToString(" ")

                val compatiblePackage = patch.compatiblePackages?.first()
                val latestVersion = compatiblePackage?.versions?.maxByOrNull { it.replace(".", "").toInt() } ?: "all"

                table.appendLine("|$humanName|${patch.description}|`${compatiblePackage?.name}`|$latestVersion|")
            }

            val readMeTemplateFile = File("README-template.md")
            val readMeTemplate = Template(readMeTemplateFile.readText())

            readMeTemplate.replaceVariable("table", table.toString())

            val readMeFile = File("README.md")
            readMeFile.writeText(readMeTemplate.toString())
        }
    }
}
