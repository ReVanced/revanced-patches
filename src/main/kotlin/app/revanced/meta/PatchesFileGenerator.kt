package app.revanced.meta

import app.revanced.patcher.data.Context
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.util.patch.PatchBundle
import java.io.File

internal typealias PatchBundlePatches = List<Class<out Patch<Context>>>

internal interface PatchesFileGenerator {
    fun generate(bundle: PatchBundlePatches)

    private companion object {
        @JvmStatic
        fun main(args: Array<String>) = PatchBundle.Jar(
            File("build/libs/").listFiles()!!.first {
                it.name.startsWith("revanced-patches-") && it.name.endsWith(".jar")
            }.absolutePath
        ).loadPatches().also {
            if (it.isEmpty()) throw IllegalStateException("No patches found")
        }.let { bundle ->
            arrayOf(JsonGenerator(), ReadmeGenerator()).forEach { it.generate(bundle) }
        }
    }
}