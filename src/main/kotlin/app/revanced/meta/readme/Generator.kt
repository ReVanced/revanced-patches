package app.revanced.meta.readme

import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.description
import app.revanced.patcher.extensions.PatchExtensions.patchName
import app.revanced.patcher.util.patch.implementation.JarPatchBundle
import java.io.File

class Generator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val buildDir = File("build/libs/")
            val buildJar =
                buildDir.listFiles()?.first { it.name.startsWith("revanced-patches-") && it.name.endsWith(".jar") }!!

            val bundle = JarPatchBundle(buildJar.absolutePath).loadPatches()

            val patches = StringBuilder()

            for (patch in bundle) {
                val patchName = patch.patchName
                val compatiblePackage = patch.compatiblePackages?.first()
                val latestVersion =
                    compatiblePackage?.versions?.map { SemanticVersion.fromString(it) }?.maxWithOrNull(
                        SemanticVersionComparator
                    ) ?: "all"

                patches.appendLine("| `$patchName` | ${patch.description} | `${compatiblePackage?.name}` | $latestVersion |")
            }

            val readMeTemplateFile = File("README-template.md")
            val readmeTemplate = Template(readMeTemplateFile.readText())

            readmeTemplate.replaceVariable("table", patches.toString())

            val readme = File("README.md")
            readme.writeText(readmeTemplate.toString())
        }
    }
}
