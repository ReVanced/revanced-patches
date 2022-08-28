package app.revanced.meta

import app.revanced.meta.json.generateJson
import app.revanced.meta.readme.generateText
import app.revanced.patcher.data.Data
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.util.patch.impl.JarPatchBundle
import java.io.File

typealias Bundle = List<Class<out Patch<Data>>>

object Meta {
    @JvmStatic
    fun main(args: Array<String>) {
        val patches = accumulatePatches()
        if (patches.isEmpty()) throw IllegalStateException("No patches found")

        generateText(patches)
        generateJson(patches)
    }
}

fun accumulatePatches() = JarPatchBundle(
    File("build/libs/").listFiles()!!.first {
        it.name.startsWith("revanced-patches-") && it.name.endsWith(".jar")
    }.absolutePath
).loadPatches()