@file:Suppress("ArrayInDataClass") // We don't need it here.

package app.revanced.meta.json

data class JsonPatch(
    val name: String,
    val description: String,
    val version: String,
    val excluded: Boolean,
    val deprecated: Boolean,
    val options: Array<Option>,
    val dependencies: Array<String>,
    val compatiblePackages: Array<CompatiblePackage>,
)

data class CompatiblePackage(
    val name: String,
    val versions: Array<String>,
)

data class Option(
    val key: String,
    val title: String,
    val description: String,
    val required: Boolean,
    val choices: Array<*>?,
)