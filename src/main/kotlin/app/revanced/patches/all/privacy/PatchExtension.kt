package app.revanced.patches.all.privacy

import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.options.PatchOption
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.booleanPatchOption
import kotlin.properties.ReadOnlyProperty

internal fun Patch<*>.option(
    title: String,
) = ReadOnlyProperty<Any?, PatchOption<Boolean?>> { _, property ->
    booleanPatchOption(key = property.name, default = true, title = title, required = true)
}
