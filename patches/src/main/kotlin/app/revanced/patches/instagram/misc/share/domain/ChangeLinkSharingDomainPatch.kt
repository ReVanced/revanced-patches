package app.revanced.patches.instagram.misc.share.domain

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.patches.instagram.misc.share.editShareLinksPatch
import app.revanced.util.returnEarly

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/instagram/misc/share/domain/ChangeLinkSharingDomainPatch;"

@Suppress("unused")
val `Change link sharing domain` by creatingBytecodePatch(
    description = "Replaces the domain name of shared links.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    val customDomainHost by stringOption(
        default = "imginn.com",
        name = "Domain name",
        description = "The domain name to use when sharing links.",
    )

    apply {
        getCustomShareDomainMethod.returnEarly(customDomainHost!!)

        editShareLinksPatch { index, register ->
            addInstructions(
                index,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->setCustomShareDomain(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$register
                """,
            )
        }
    }
}
