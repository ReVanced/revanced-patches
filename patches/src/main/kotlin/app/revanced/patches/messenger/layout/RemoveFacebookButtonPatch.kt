package app.revanced.patches.messenger.layout

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.config.appFeatureFlagsPatch
import app.revanced.patches.messenger.config.addAppFeatureFlagsOverrider

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/layout/FacebookButtonDisabler;"

@Suppress("unused")
val removeFacebookButtonPatch = bytecodePatch(
    name = "Remove Facebook button",
    description = "Removes the Facebook button in the top right corner."
) {
    compatibleWith("com.facebook.orca")

    dependsOn(appFeatureFlagsPatch)

    execute {
        addAppFeatureFlagsOverrider(EXTENSION_CLASS_DESCRIPTOR)
    }
}