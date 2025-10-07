package app.revanced.patches.instagram.misc.share.domain

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.patches.instagram.misc.share.editShareLinksPatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_CHANGE_LINK_SHARING_DOMAIN
import app.revanced.patches.shared.PATCH_NAME_CHANGE_LINK_SHARING_DOMAIN
import app.revanced.util.returnEarly

@Suppress("unused")
val changeLinkSharingDomainPatch = bytecodePatch(
    name = PATCH_NAME_CHANGE_LINK_SHARING_DOMAIN,
    description = PATCH_DESCRIPTION_CHANGE_LINK_SHARING_DOMAIN,
    use = false
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    execute {
        val customDomainHost by stringOption(
            key = "domainName",
            default = "imginn.com",
            title = "Domain name",
            description = "The domain name to use when sharing links."
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
