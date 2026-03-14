package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/spotify/misc/privacy/SanitizeSharingLinksPatch;"

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    apply {
        val extensionMethodDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->" +
            "sanitizeSharingLink(Ljava/lang/String;)Ljava/lang/String;"

        val copyMethod = if (shareCopyUrlMethod != null) {
            shareCopyUrlMethod
        } else {
            oldShareCopyUrlMethod
        }

        copyMethod!!.apply {
            val newPlainTextInvokeIndex = indexOfFirstInstructionOrThrow {
                methodReference?.name == "newPlainText"
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
        val shareSheetMethod = if (formatAndroidShareSheetUrlMethod != null) {
            shareUrlParameter = if (AccessFlags.STATIC.isSet(formatAndroidShareSheetUrlMethod!!.accessFlags)) {
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

        shareSheetMethod!!.addInstructions(
            0,
            """
                invoke-static { $shareUrlParameter }, $extensionMethodDescriptor
                move-result-object $shareUrlParameter
            """,
        )
    }
}
