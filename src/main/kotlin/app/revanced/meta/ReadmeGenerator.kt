package app.revanced.patches.meta

import java.io.File
import kotlin.io.writeText
import kotlin.collections.first
import app.revanced.patcher.util.patch.implementation.JarPatchBundle
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.patchName
import app.revanced.patcher.extensions.PatchExtensions.description

class ReadmeGenerator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //should be moved to a file?
            val generalReadme =
                """    
            # ReVanced Patches
            🧩 Official patches by ReVanced

            # Patch list
            """.trimIndent()

            val tableHeader =
                """
            | 💊 Patch | 📜 Description | 🎯 Target Package | 🏹 Target Version |
            |:-----:|:-----------:|:--------------:|:----------------------:|
            """.trimIndent()

            val readmeFile = File("README.md")

            val buildDir = File("build/libs/")
            val buildJar = buildDir.listFiles().first { it.name.startsWith("revanced-patches-") && it.name.endsWith(".jar") }

            val bundle = JarPatchBundle(buildJar.absolutePath).loadPatches()

            val builder = StringBuilder()

            builder.appendLine(generalReadme)
            builder.appendLine(tableHeader)

            for (patch in bundle) {
                val humanName =
                    patch.patchName.split('-').map { it.replaceFirstChar { it.uppercase() } }.joinToString(" ")

                val compatiblePackage = patch.compatiblePackages?.first()
                val latestVersion = compatiblePackage?.versions?.maxByOrNull { it.replace(".", "").toInt() } ?: "all"

                builder.appendLine("|$humanName|${patch.description}|`${compatiblePackage?.name}`|$latestVersion|")
            }

            readmeFile.writeText(builder.toString())
        }
    }
}
