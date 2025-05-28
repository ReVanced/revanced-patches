package app.revanced.patches.messenger.metaai

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.config.appFeatureFlagsPatch
import app.revanced.patches.messenger.config.addAppFeatureFlagsOverrider

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/metaai/MetaAIDisabler;"

@Suppress("unused")
val removeMetaAIPatch = bytecodePatch(
    name = "Remove Meta AI",
    description = "Removes UI elements related to Meta AI."
) {
    compatibleWith("com.facebook.orca")

    dependsOn(appFeatureFlagsPatch)

    execute {
        addAppFeatureFlagsOverrider(EXTENSION_CLASS_DESCRIPTOR)
    }
}