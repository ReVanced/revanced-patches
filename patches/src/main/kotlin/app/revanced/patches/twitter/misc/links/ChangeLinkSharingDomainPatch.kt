package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var tweetShareLinkTemplateId = -1L
    private set

internal val changeLinkSharingDomainResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        tweetShareLinkTemplateId = resourceMappings["string", "tweet_share_link"]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/twitter/patches/links/ChangeLinkSharingDomainPatch;"

@Suppress("unused")
val changeLinkSharingDomainPatch = bytecodePatch(
    name = "Change link sharing domain",
    description = "Replaces the domain name of Twitter links when sharing them.",
) {
    dependsOn(
        changeLinkSharingDomainResourcePatch,
        sharedExtensionPatch,
    )

    compatibleWith("com.twitter.android")

    val domainName by stringOption(
        key = "domainName",
        default = "fxtwitter.com",
        title = "Domain name",
        description = "The domain name to use when sharing links.",
        required = true,
    )

    execute {
        val replacementIndex =
            linkSharingDomainFingerprint.stringMatches.first().index
        val domainRegister =
            linkSharingDomainFingerprint.method.getInstruction<OneRegisterInstruction>(replacementIndex).registerA

        linkSharingDomainFingerprint.method.replaceInstruction(
            replacementIndex,
            "const-string v$domainRegister, \"https://$domainName\"",
        )

        // Replace the domain name when copying a link with "Copy link" button.
        linkBuilderFingerprint.method.apply {
            addInstructions(
                0,
                """
                    invoke-static { p0, p1, p2 }, $EXTENSION_CLASS_DESCRIPTOR->formatLink(JLjava/lang/String;)Ljava/lang/String;
                    move-result-object p0
                    return-object p0
                """,
            )
        }

        // Used in the Share via... dialog.
        linkResourceGetterFingerprint.method.apply {
            val templateIdConstIndex = indexOfFirstLiteralInstructionOrThrow(tweetShareLinkTemplateId)

            // Format the link with the new domain name register (1 instruction below the const).
            val formatLinkCallIndex = templateIdConstIndex + 1
            val register = getInstruction<FiveRegisterInstruction>(formatLinkCallIndex).registerE

            // Replace the original method call with the new method call.
            replaceInstruction(
                formatLinkCallIndex,
                "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->formatResourceLink([Ljava/lang/Object;)Ljava/lang/String;",
            )
        }
    }
}
