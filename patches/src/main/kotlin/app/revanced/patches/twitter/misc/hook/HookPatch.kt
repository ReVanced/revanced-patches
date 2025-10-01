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

    compatibleWith(
        "com.twitter.android"(
            "10.60.0-release.0",
            "10.86.0-release.0",
        )
    )

    execute {
        addJsonHook(JsonHook(hookClassDescriptor))
    }
}
