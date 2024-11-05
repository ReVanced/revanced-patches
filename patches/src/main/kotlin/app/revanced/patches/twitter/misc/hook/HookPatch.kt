package app.revanced.patches.twitter.misc.hook

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.twitter.misc.hook.json.JsonHook
import app.revanced.patches.twitter.misc.hook.json.addJsonHook
import app.revanced.patches.twitter.misc.hook.json.jsonHookPatch

fun hookPatch(
    name: String,
    hookClassDescriptor: String,
) = bytecodePatch(name) {
    dependsOn(jsonHookPatch)

    compatibleWith("com.twitter.android")

    execute {
        addJsonHook(JsonHook(hookClassDescriptor))
    }
}
