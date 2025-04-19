package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch

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
        with(shareUrlToStringFingerprint) {
            classDef.methods.first { it.name == "<init>" }.addInstructions(
                0,
                """
                    invoke-static {p1}, $EXTENSION_CLASS_DESCRIPTOR->sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;
                    
                    move-result-object p1
                    
                    invoke-static {p4}, $EXTENSION_CLASS_DESCRIPTOR->sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;
                    
                    move-result-object p4
                """
            )
        }
    }
}