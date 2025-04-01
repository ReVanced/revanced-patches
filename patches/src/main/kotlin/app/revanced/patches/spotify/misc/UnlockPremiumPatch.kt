package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/premium/UnlockPremiumPatch;"

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

        // Enable choosing a specific song/artist via Voice assistant.
        contextFromJsonFingerprint.method.apply {
            val insertIndex = contextFromJsonFingerprint.patternMatch!!.startIndex
            val registerUrl = getInstruction<FiveRegisterInstruction>(insertIndex).registerC
            val registerUri = getInstruction<FiveRegisterInstruction>(insertIndex + 2).registerD

            addInstructions(
                insertIndex,
                """
                invoke-static { v$registerUrl }, $EXTENSION_CLASS_DESCRIPTOR->removeStationString(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$registerUrl
                invoke-static { v$registerUri }, $EXTENSION_CLASS_DESCRIPTOR->removeStationString(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$registerUri
                """
            )
        }

        // Removed forced shuffle when asking for an album/playlist via Voice assistant.
        readPlayerOptionOverridesFingerprint.method.apply {
            val shufflingContextCallIndex = instructions.indexOfFirst { instruction ->
                if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return@indexOfFirst false

                val invokeInstruction = instruction as Instruction35c
                val methodRef = invokeInstruction.reference as MethodReference

                if (methodRef.name != "shufflingContext") {
                    return@indexOfFirst false
                }
                true
            }

            val registerBool = getInstruction<FiveRegisterInstruction>(shufflingContextCallIndex).registerD
            addInstructions(
                shufflingContextCallIndex,
                """
                const/4 v${registerBool}, 0x0
                invoke-static {v${registerBool}}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v${registerBool}
                """
            )
        }
    }
}
