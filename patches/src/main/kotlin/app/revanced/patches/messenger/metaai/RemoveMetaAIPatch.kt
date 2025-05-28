package app.revanced.patches.messenger.metaai

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.config.overrideMobileConfigPatch
import app.revanced.patches.messenger.config.addConfigOverrider

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/metaai/MetaAIConfigDisabler;"

@Suppress("unused")
val removeMetaAIPatch = bytecodePatch(
    name = "Remove Meta AI",
    description = "Removes UI elements related to Meta AI."
) {
    compatibleWith("com.facebook.orca")

    dependsOn(overrideMobileConfigPatch)

    execute {
        addConfigOverrider(EXTENSION_CLASS_DESCRIPTOR)
    }
}