package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/spotify/misc/privacy/SanitizeSharingLinksPatch;"

@Suppress("unused", "ObjectPropertyName")
val `Sanitize sharing links` by creatingBytecodePatch(
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    apply {
        val extensionMethodDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->" +
            "sanitizeSharingLink(Ljava/lang/String;)Ljava/lang/String;"

        val copyFingerprint = if (shareCopyUrlMethod.originalMethodOrNull != null) {
            shareCopyUrlMethod
        } else {
            oldShareCopyUrlMethod
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
                """,
            )
        }

        // Android native share sheet is used for all other quick share types (X, WhatsApp, etc).
        val shareUrlParameter: String
        val shareSheetFingerprint = if (formatAndroidShareSheetUrlMethod.originalMethodOrNull != null) {
            val methodAccessFlags = formatAndroidShareSheetUrlMethod.originalMethod
            shareUrlParameter = if (AccessFlags.STATIC.isSet(methodAccessFlags.accessFlags)) {
                // In newer implementations the method is static, so p0 is not `this`.
                "p1"
            } else {
                // In older implementations the method is not static, making it so p0 is `this`.
                // For that reason, add one to the parameter register.
                "p2"
            }

            formatAndroidShareSheetUrlMethod
        } else {
            shareUrlParameter = "p2"
            oldFormatAndroidShareSheetUrlMethod
        }

        shareSheetFingerprint.method.addInstructions(
            0,
            """
                invoke-static { $shareUrlParameter }, $extensionMethodDescriptor
                move-result-object $shareUrlParameter
            """,
        )
    }
}
