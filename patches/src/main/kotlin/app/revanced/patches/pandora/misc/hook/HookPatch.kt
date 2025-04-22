package app.revanced.patches.pandora.misc.hook

import app.revanced.patcher.patch.bytecodePatch

fun hookPatch(
    name: String,
    hookClassDescriptor: String,
) = bytecodePatch(name) {
    dependsOn(jsonHookPatch)

    compatibleWith("com.pandora.android")

    execute {
        addJsonHook(JsonHook(hookClassDescriptor))
    }
}