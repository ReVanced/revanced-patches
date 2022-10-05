package app.revanced.meta

import app.revanced.meta.json.generateJson
import app.revanced.meta.readme.generateText
import app.revanced.patcher.data.Context
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.util.patch.PatchBundle
import java.io.File

typealias Bundle = List<Class<out Patch<Context>>>

object Meta {
    @JvmStatic
    fun main(args: Array<String>) {
        val patches = accumulatePatches()
        if (patches.isEmpty()) throw IllegalStateException("No patches found")

        generateText(patches)
        generateJson(patches)
    }
}

fun accumulatePatches() = PatchBundle.Jar(
    File("build/libs/").listFiles()!!.first {
        it.name.startsWith("revanced-patches-") && it.name.endsWith(".jar")
    }.absolutePath
).loadPatches()