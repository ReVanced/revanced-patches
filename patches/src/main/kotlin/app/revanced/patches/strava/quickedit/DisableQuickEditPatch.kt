package app.revanced.patches.strava.quickedit

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableQuickEditPatch = bytecodePatch(
    name = "Disable Quick Edit",
    description = "Prevents the Quick Edit prompt from popping up.",
) {
    compatibleWith("com.strava")

    execute {
        getHasAccessToQuickEditFingerprint.method.returnEarly()
    }
}
