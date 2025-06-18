package app.revanced.patches.messenger.metaai

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/metaai/RemoveMetaAIPatch;"
internal const val EXTENSION_METHOD_NAME = "overrideBooleanFlag"

@Suppress("unused")
val removeMetaAIPatch = bytecodePatch(
    name = "Remove Meta AI",
    description = "Removes UI elements related to Meta AI."
) {
    compatibleWith("com.facebook.orca")

    dependsOn(sharedExtensionPatch)

    execute {
        getMobileConfigBoolFingerprint.method.apply {
            val returnIndex = getMobileConfigBoolFingerprint.patternMatch!!.startIndex
            val returnRegister = getInstruction<OneRegisterInstruction>(returnIndex).registerA

            addInstructions(
                returnIndex,
                """
                    invoke-static { p1, p2, v$returnRegister }, $EXTENSION_CLASS_DESCRIPTOR->$EXTENSION_METHOD_NAME(JZ)Z
                    move-result v$returnRegister
                """
            )
        }

        // Extract the common starting digits of Meta AI flag IDs from a flag found in code.
        val relevantDigits = with(metaAIKillSwitchCheckFingerprint) {
            method.getInstruction<WideLiteralInstruction>(patternMatch!!.startIndex).wideLiteral
        }.toString().substring(0, 7)

        // Replace placeholder in the extension method.
        with(extensionMethodFingerprint) {
            method.replaceInstruction(
                stringMatches!!.first().index,
                """
                    const-string v1, "$relevantDigits"
                """
            )
        }
    }
}
