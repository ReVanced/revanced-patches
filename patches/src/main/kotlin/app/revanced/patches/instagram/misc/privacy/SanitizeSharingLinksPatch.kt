package app.revanced.patches.instagram.misc.privacy

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS
import app.revanced.patches.shared.PATCH_NAME_SANITIZE_SHARING_LINKS
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/instagram/misc/privacy/SanitizeSharingLinksPatch;"

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = PATCH_NAME_SANITIZE_SHARING_LINKS,
    description = PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS,
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    execute {
        fun Fingerprint.sanitizeUrl() {
            this.method.apply {
                val putSharingUrlIndex = indexOfFirstInstructionOrThrow(
                    this@sanitizeUrl.stringMatches!!.first().index,
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

        val fingerprintsToPatch = arrayOf(permalinkResponseJsonParserFingerprint,
            storyUrlResponseJsonParserFingerprint,
            profileUrlResponseJsonParserFingerprint,
            liveUrlResponseJsonParserFingerprint
        )

        for(f in fingerprintsToPatch)
            f.sanitizeUrl()
    }
}
