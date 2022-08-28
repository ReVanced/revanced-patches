@file:Suppress("ArrayInDataClass") // We don't need it here.

package app.revanced.meta.json

data class JsonPatch(
    val name: String,
    val description: String,
    val version: String,
    val excluded: Boolean,
    val dependencies: Array<String>,
    val compatiblePackages: Array<CompatiblePackage>,
)

data class CompatiblePackage(
    val name: String,
    val versions: Array<String>,
)