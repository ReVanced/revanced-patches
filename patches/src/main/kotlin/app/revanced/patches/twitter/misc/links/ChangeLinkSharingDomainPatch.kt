package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.logging.Logger

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/twitter/patches/links/ChangeLinkSharingDomainPatch;"

internal var tweetShareLinkTemplateId = -1L
    private set

@Suppress("unused")
val changeLinkSharingDomainPatch = bytecodePatch(
    name = "Change link sharing domain",
    description = "Replaces the domain name of Twitter links when sharing them.",
) {
    dependsOn(
        resourceMappingPatch,
        sharedExtensionPatch,
    )

    compatibleWith(
        "com.twitter.android"(
            "10.86.0-release.0",
            "10.60.0-release.0",
            "10.48.0-release.0"
        )
    )

    val domainName by stringOption(
        key = "domainName",
        default = "fxtwitter.com",
        title = "Domain name",
        description = "The domain name to use when sharing links.",
        required = true,
    ) {
        // Do a courtesy check if the host can be resolved.
        // If it does not resolve, then print a warning but use the host anyway.
        // Unresolvable hosts should not be rejected, since the patching environment
        // may not allow network connections or the network may be down.
        try {
            InetAddress.getByName(it)
        } catch (e: UnknownHostException) {
            Logger.getLogger(this::class.java.name).warning(
                "Host \"$it\" did not resolve to any domain."
            )
        }
        true
    }

    execute {
        tweetShareLinkTemplateId = resourceMappings["string", "tweet_share_link"]

        // Replace the domain name in the link sharing extension methods.
        linkSharingDomainHelperFingerprint.method.returnEarly("https://$domainName")

        // Replace the domain name when copying a link with "Copy link" button.
        linkBuilderFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p0, p1, p2 }, $EXTENSION_CLASS_DESCRIPTOR->formatLink(JLjava/lang/String;)Ljava/lang/String;
                move-result-object p0
                return-object p0
            """
        )

        // Used in the Share via... dialog.
        linkResourceGetterFingerprint.method.apply {
            val templateIdConstIndex = indexOfFirstLiteralInstructionOrThrow(tweetShareLinkTemplateId)

            // Format the link with the new domain name register (1 instruction below the const).
            val formatLinkCallIndex = templateIdConstIndex + 1
            val register = getInstruction<FiveRegisterInstruction>(formatLinkCallIndex).registerE

            // Replace the original method call with the new method call.
            replaceInstruction(
                formatLinkCallIndex,
                "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->" +
                        "formatResourceLink([Ljava/lang/Object;)Ljava/lang/String;",
            )
        }
    }
}
