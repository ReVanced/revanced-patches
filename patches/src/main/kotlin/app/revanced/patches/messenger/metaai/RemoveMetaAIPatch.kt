package app.revanced.patches.messenger.metaai

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/metaai/RemoveMetaAIPatch;"

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
                    invoke-static { p1, p2, v$returnRegister }, $EXTENSION_CLASS_DESCRIPTOR->overrideBooleanFlag(JZ)Z
                    move-result v$returnRegister
                """
            )
        }
    }
}
