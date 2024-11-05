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
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

internal var tweetShareLinkTemplateId = -1L
    private set

internal val changeLinkSharingDomainResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        tweetShareLinkTemplateId = resourceMappings["string", "tweet_share_link"]
    }
}

// This method is used to build the link that is shared when the "Share via..." button is pressed.
private const val FORMAT_METHOD_RESOURCE_REFERENCE =
    "Lapp/revanced/extension/twitter/patches/links/ChangeLinkSharingDomainPatch;->" +
        "formatResourceLink([Ljava/lang/Object;)Ljava/lang/String;"

// This method is used to build the link that is shared when the "Copy link" button is pressed.
private const val FORMAT_METHOD_REFERENCE =
    "Lapp/revanced/extension/twitter/patches/links/ChangeLinkSharingDomainPatch;->" +
        "formatLink(JLjava/lang/String;)Ljava/lang/String;"

@Suppress("unused")
val changeLinkSharingDomainPatch = bytecodePatch(
    name = "Change link sharing domain",
    description = "Replaces the domain name of Twitter links when sharing them.",
) {
    dependsOn(changeLinkSharingDomainResourcePatch)

    compatibleWith("com.twitter.android")

    val domainName by stringOption(
        key = "domainName",
        default = "fxtwitter.com",
        title = "Domain name",
        description = "The domain name to use when sharing links.",
        required = true,
    )

    val linkSharingDomainMatch by linkSharingDomainFingerprint()
    val linkBuilderMatch by linkBuilderFingerprint()
    val linkResourceGetterMatch by linkResourceGetterFingerprint()

    execute {
        val replacementIndex =
            linkSharingDomainMatch.stringMatches!!.first().index
        val domainRegister =
            linkSharingDomainMatch.mutableMethod.getInstruction<OneRegisterInstruction>(replacementIndex).registerA

        linkSharingDomainMatch.mutableMethod.replaceInstruction(
            replacementIndex,
            "const-string v$domainRegister, \"https://$domainName\"",
        )

        // Replace the domain name when copying a link with "Copy link" button.
        linkBuilderMatch.mutableMethod.apply {
            addInstructions(
                0,
                """
                    invoke-static { p0, p1, p2 }, $FORMAT_METHOD_REFERENCE
                    move-result-object p0
                    return-object p0
                """,
            )
        }

        // Used in the Share via... dialog.
        linkResourceGetterMatch.mutableMethod.apply {
            val templateIdConstIndex = indexOfFirstLiteralInstructionOrThrow(tweetShareLinkTemplateId)

            // Format the link with the new domain name register (1 instruction below the const).
            val formatLinkCallIndex = templateIdConstIndex + 1
            val formatLinkCall = getInstruction<Instruction35c>(formatLinkCallIndex)

            // Replace the original method call with the new method call.
            replaceInstruction(
                formatLinkCallIndex,
                "invoke-static { v${formatLinkCall.registerE} }, $FORMAT_METHOD_RESOURCE_REFERENCE",
            )
        }
    }
}