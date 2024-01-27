package app.revanced.meta

import app.revanced.patcher.PatchBundleLoader
import app.revanced.patcher.PatchSet
import java.io.File

internal interface IPatchesFileGenerator {
    fun generate(patches: PatchSet)

    private companion object {
        @JvmStatic
        fun main(args: Array<String>) = PatchBundleLoader.Jar(
            File("build/libs/").listFiles { it -> it.name.endsWith(".jar") }!!.first()
        ).also { loader ->
            if (loader.isEmpty()) throw IllegalStateException("No patches found")
        }.let { bundle ->
            arrayOf(JsonPatchesFileGenerator()).forEach { generator -> generator.generate(bundle) }
        }
    }
}
