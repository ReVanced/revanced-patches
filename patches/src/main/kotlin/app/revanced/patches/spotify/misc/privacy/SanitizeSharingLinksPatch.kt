package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.patches.spotify.shared.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/spotify/misc/privacy/SanitizeSharingLinksPatch;"

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from links before they are shared.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    execute {
        val extensionMethodDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->" +
                "sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;"

        val copyFingerprint = if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            shareCopyUrlLegacyFingerprint
        } else {
            shareCopyUrlFingerprint
        }

        copyFingerprint.method.apply {
            val newPlainTextInvokeIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.name == "newPlainText"
            }
            val urlRegister = getInstruction<FiveRegisterInstruction>(newPlainTextInvokeIndex).registerD

            addInstructions(
                newPlainTextInvokeIndex,
                """
                    invoke-static { v$urlRegister }, $extensionMethodDescriptor
                    move-result-object v$urlRegister
                """
            )
        }

        // Android native share sheet is used for all other quick share types (X, WhatsApp, etc).
        val shareUrlParameter : String
        val shareSheetFingerprint : Fingerprint
        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            shareSheetFingerprint = formatAndroidShareSheetUrlLegacyFingerprint
            shareUrlParameter = "p2"
        } else {
            shareSheetFingerprint = formatAndroidShareSheetUrlFingerprint
            shareUrlParameter = "p1"
        }

        shareSheetFingerprint.method.addInstructions(
            0,
            """
                invoke-static { $shareUrlParameter }, $extensionMethodDescriptor
                move-result-object $shareUrlParameter
            """
        )
    }
}
