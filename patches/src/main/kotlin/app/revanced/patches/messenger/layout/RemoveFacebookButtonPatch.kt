package app.revanced.patches.messenger.layout

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.config.overrideMobileConfigPatch
import app.revanced.patches.messenger.config.addConfigOverrider

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/layout/FacebookButtonConfigDisabler;"

@Suppress("unused")
val removeFacebookButtonPatch = bytecodePatch(
    name = "Remove Facebook Button",
    description = "Removes the Facebook button in the top right corner."
) {
    compatibleWith("com.facebook.orca")

    dependsOn(overrideMobileConfigPatch)

    execute {
        addConfigOverrider(EXTENSION_CLASS_DESCRIPTOR)
    }
}