package app.revanced.patches.twitter.misc.hook

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.twitter.misc.hook.json.JsonHook
import app.revanced.patches.twitter.misc.hook.json.jsonHookPatch
import app.revanced.patches.twitter.misc.hook.json.jsonHooks

fun hookPatch(
    name: String,
    hookClassDescriptor: String,
) = bytecodePatch(name) {
    dependsOn(jsonHookPatch)

    compatibleWith("com.twitter.android")

    execute {
        jsonHooks.addHook(JsonHook(it, hookClassDescriptor))
    }
}
