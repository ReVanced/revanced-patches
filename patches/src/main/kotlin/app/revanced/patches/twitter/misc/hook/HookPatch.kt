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
            // 10.85+ uses Pairip and requires additional changes to work.
            "10.84.0-release.0",
            // Confirmed to not show reply ads. Slightly newer versions may also work.
            "10.60.0-release.0",
            "10.48.0-release.0"
        )
    )

    execute {
        addJsonHook(JsonHook(hookClassDescriptor))
    }
}
