package app.revanced.patches.instagram.misc.share.domain

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.patches.instagram.misc.share.editShareLinksPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val setCustomShareDomainPatch = bytecodePatch(
    name = "Set custom share domain",
    description = "Removes the tracking query parameters from shared links.", //TODO
    use = false
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    execute {
        val customDomainHost by stringOption(
            key = "customSearchDomain",
            default = "imginn.com",
            title = "Custom share domain",
            description = "Permanently hides the Reels button." //TODO
        )

        getCustomShareDomainFingerprint.method.returnEarly(customDomainHost!!)

        editShareLinksPatch { index, register ->
            addInstructions(
                index,
                """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->setCustomShareDomain(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """
            )
        }
    }
}
