package app.revanced.meta.json

import app.revanced.meta.Bundle
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.dependencies
import app.revanced.patcher.extensions.PatchExtensions.description
import app.revanced.patcher.extensions.PatchExtensions.include
import app.revanced.patcher.extensions.PatchExtensions.patchName
import app.revanced.patcher.extensions.PatchExtensions.version
import com.google.gson.Gson
import java.io.File

private val gson = Gson()

fun generateJson(bundle: Bundle) {
    val patches = bundle.map {
        JsonPatch(
            it.patchName,
            it.description ?: "This patch has no description.",
            it.version ?: "0.0.0",
            !it.include,
            it.dependencies?.map { dep ->
                dep.java.patchName
            }?.toTypedArray() ?: emptyArray(),
            it.compatiblePackages?.map { pkg ->
                CompatiblePackage(pkg.name, pkg.versions)
            }?.toTypedArray() ?: emptyArray()
        )
    }

    val json = File("patches.json")
    json.writeText(gson.toJson(patches))
}