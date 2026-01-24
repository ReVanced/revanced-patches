package app.revanced.patches.strava.quickedit

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Disable Quick Edit` by creatingBytecodePatch(
    description = "Prevents the Quick Edit prompt from popping up.",
) {
    compatibleWith("com.strava")

    apply {
        getHasAccessToQuickEditMethod.returnEarly()
    }
}
