package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
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

        val copyMethod = shareCopyUrlFingerprint.methodOrNull ?: oldShareCopyUrlFingerprint.method

        copyMethod.apply {
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
        var shareUrlParameter = ""
        val shareSheetMethod = formatAndroidShareSheetUrlFingerprint.methodOrNull?.also {
            val methodAccessFlags = formatAndroidShareSheetUrlFingerprint.originalMethod.accessFlags
            shareUrlParameter = if (AccessFlags.STATIC.isSet(methodAccessFlags)) {
                // In newer implementations the method is static, so p0 is not `this`.
                "p1"
            } else {
                // In older implementations the method is not static, making it so p0 is `this`.
                // For that reason, add one to the parameter register.
                "p2"
            }
        } ?: oldFormatAndroidShareSheetUrlFingerprint.method.also {
            shareUrlParameter = "p2"
        }

        shareSheetMethod.addInstructions(
            0,
            """
                invoke-static { $shareUrlParameter }, $extensionMethodDescriptor
                move-result-object $shareUrlParameter
            """
        )
    }
}
