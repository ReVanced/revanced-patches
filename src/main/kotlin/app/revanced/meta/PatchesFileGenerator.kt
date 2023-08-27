package app.revanced.meta

import app.revanced.patcher.PatchBundleLoader
import app.revanced.patcher.patch.PatchClass
import java.io.File

internal typealias PatchBundlePatches = List<PatchClass>

internal interface PatchesFileGenerator {
    fun generate(bundle: PatchBundlePatches)

    private companion object {
        @JvmStatic
        fun main(args: Array<String>) = PatchBundleLoader.Jar(
            File("build/libs/").listFiles { it -> it.name.endsWith(".jar") }!!.first()
        ).also { loader ->
            if (loader.isEmpty()) throw IllegalStateException("No patches found")
        }.let { bundle ->
            arrayOf(JsonGenerator()).forEach { generator -> generator.generate(bundle) }
        }
    }
}