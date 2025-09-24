package app.revanced.patches.instagram.misc.share.domain

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.patches.instagram.misc.share.editShareLinksPatch
import app.revanced.patches.instagram.misc.share.permalinkResponseJsonParserFingerprint


private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/instagram/misc/share/domain/CustomShareDomainPatch;"

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = "Custom share domain",
    description = "Removes the tracking query parameters from shared links.", //TODO
    use = false
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    execute {
        val customDomainHost by stringOption(
            key = "customSearchDomain",
            default = "imginn.com",
            title = "Custom search domain",
            description = "Permanently hides the Reels button." //TODO
        )

        with(permalinkResponseJsonParserFingerprint.method) {
            editShareLinksPatch { index, register ->
                val freeRegister = 4
                addInstructions(
                    index,
                    """
                        const-string v$freeRegister, "$customDomainHost"
                        invoke-static { v$register, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->setCustomShareDomain(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """
                )
            }
        }
    }
}
