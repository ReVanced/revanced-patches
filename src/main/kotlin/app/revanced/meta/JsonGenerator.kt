package app.revanced.meta

import app.revanced.patcher.PatchSet
import app.revanced.patcher.patch.Patch
import com.google.gson.GsonBuilder
import java.io.File

internal class JsonGenerator : PatchesFileGenerator {
    override fun generate(patches: PatchSet) = patches.map {
        JsonPatch(
            it.name!!,
            it.description,
            it.compatiblePackages,
            it.use,
            it.requiresIntegrations,
            it.options.values.map { option ->
                JsonPatch.Option(option.key, option.value, option.title, option.description, option.required)
            }
        )
    }.let {
        File("patches.json").writeText(GsonBuilder().serializeNulls().create().toJson(it))
    }

    @Suppress("unused")
    private class JsonPatch(
        val name: String? = null,
        val description: String? = null,
        val compatiblePackages: Set<Patch.CompatiblePackage>? = null,
        val use: Boolean = true,
        val requiresIntegrations: Boolean = false,
        val options: List<Option>
    ) {
        class Option(
            val key: String,
            val default: Any?,
            val title: String?,
            val description: String?,
            val required: Boolean,
        )
    }
}