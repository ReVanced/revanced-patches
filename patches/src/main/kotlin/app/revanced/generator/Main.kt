package app.revanced.generator

import app.revanced.patcher.patch.loadPatchesFromJar
import java.io.File

internal fun main() = loadPatchesFromJar(
    setOf(File("build/libs/").listFiles { it -> it.name.endsWith(".jar") }!!.first()),
).also { loader ->
    if (loader.isEmpty()) throw IllegalStateException("No patches found")
}.let { bundle ->
    arrayOf(JsonPatchesFileGenerator()).forEach { generator -> generator.generate(bundle) }
}
