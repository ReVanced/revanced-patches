package app.revanced.patches.instagram.misc.privacy

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/instagram/misc/privacy/SanitizeSharingLinksPatch;"

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    execute {
        permalinkResponseJsonParserFingerprint.method.apply {
            val putSharingUrlIndex = indexOfFirstInstruction(
                permalinkResponseJsonParserFingerprint.stringMatches!!.first { it.string == "permalink" }.index,
                Opcode.IPUT_OBJECT
            )

            val sharingUrlRegister = getInstruction<TwoRegisterInstruction>(putSharingUrlIndex).registerA

            addInstructions(
            	putSharingUrlIndex,
                """
                    invoke-static { v$sharingUrlRegister }, $EXTENSION_CLASS_DESCRIPTOR->sanitizeSharingLink(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$sharingUrlRegister
                """
            )
        }
    }
}
