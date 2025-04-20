package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
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

        androidShareSheetUrlFormatterFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, $extensionMethodDescriptor
                move-result-object p1
            """
        )

        copyUrlFormatterFingerprint.method.apply {
            val index = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.name == "newPlainText"
            }
            val register = getInstruction<FiveRegisterInstruction>(index).registerD

            addInstructions(
                index,
                """
                    invoke-static { v$register }, $extensionMethodDescriptor
                    move-result-object v$register
                """
            )
        }

        // TODO: Patch the sharing of music to X/Twitter and patch the multiple TikTok share sheets.
    }
}