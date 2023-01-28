package app.revanced.meta

import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.dependencies
import app.revanced.patcher.extensions.PatchExtensions.description
import app.revanced.patcher.extensions.PatchExtensions.include
import app.revanced.patcher.extensions.PatchExtensions.options
import app.revanced.patcher.extensions.PatchExtensions.patchName
import app.revanced.patcher.extensions.PatchExtensions.version
import app.revanced.patcher.patch.PatchOption
import com.google.gson.GsonBuilder
import java.io.File

internal class JsonGenerator : PatchesFileGenerator {
    override fun generate(bundle: PatchBundlePatches) {
        val patches = bundle.map {
            JsonPatch(
                it.patchName,
                it.description ?: "This patch has no description.",
                it.version ?: "0.0.0",
                !it.include,
                it.options?.map { option ->
                    JsonPatch.Option(
                        option.key,
                        option.title,
                        option.description,
                        option.required,
                        option.let { listOption ->
                            if (listOption is PatchOption.ListOption<*>) {
                                listOption.options.toMutableList().toTypedArray()
                            } else null
                        }
                    )
                }?.toTypedArray() ?: emptyArray(),
                it.dependencies?.map { dep ->
                    dep.java.patchName
                }?.toTypedArray() ?: emptyArray(),
                it.compatiblePackages?.map { pkg ->
                    JsonPatch.CompatiblePackage(pkg.name, pkg.versions)
                }?.toTypedArray() ?: emptyArray()
            )
        }

        val json = File("patches.json")
        json.writeText(GsonBuilder().serializeNulls().create().toJson(patches))
    }

    private class JsonPatch(
        val name: String,
        val description: String,
        val version: String,
        val excluded: Boolean,
        val options: Array<Option>,
        val dependencies: Array<String>,
        val compatiblePackages: Array<CompatiblePackage>,
    ) {
        class CompatiblePackage(
            val name: String,
            val versions: Array<String>,
        )

        class Option(
            val key: String,
            val title: String,
            val description: String,
            val required: Boolean,
            val choices: Array<*>?,
        )
    }
}