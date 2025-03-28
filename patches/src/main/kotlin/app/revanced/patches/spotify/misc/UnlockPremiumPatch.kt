package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/UnlockPremiumPatch;"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Spotify Premium",
    description = "Unlocks Spotify Premium features. Server-sided features like downloading songs are still locked.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    execute {
        // Make _value accessible so that it can be overridden in the extension.
        accountAttributeFingerprint.classDef.fields.first { it.name == "value_" }.apply {
            // Add public flag and remove private.
            accessFlags = accessFlags.or(AccessFlags.PUBLIC.value).and(AccessFlags.PRIVATE.value.inv())
        }

        // Override the attributes map in the getter method.
        val attributesMapRegister = 0
        val instantiateUnmodifiableMapIndex = 1
        productStateProtoFingerprint.method.addInstruction(
            instantiateUnmodifiableMapIndex,
            "invoke-static { v$attributesMapRegister }," +
                "$EXTENSION_CLASS_DESCRIPTOR->overrideAttribute(Ljava/util/Map;)V",
        )

        // Add the query parameter trackRows to show popular tracks in the artist page.
        val addQueryParameterIndex = buildQueryParametersFingerprint.stringMatches!!.first().index - 1
        buildQueryParametersFingerprint.method.replaceInstruction(addQueryParameterIndex, "nop")

        // Disable the "Spotify Premium" upsell experiment in context menus.
        with(contextMenuExperimentsFingerprint) {
            val moveIsEnabledIndex = stringMatches!!.first().index + 2
            val isUpsellEnabledRegister = method.getInstruction<OneRegisterInstruction>(moveIsEnabledIndex).registerA
            method.replaceInstruction(moveIsEnabledIndex, "const/4 v$isUpsellEnabledRegister, 0")
        }
    }
}
