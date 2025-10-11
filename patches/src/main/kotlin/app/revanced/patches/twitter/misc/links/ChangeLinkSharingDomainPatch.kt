package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.shared.PATCH_DESCRIPTION_CHANGE_LINK_SHARING_DOMAIN
import app.revanced.patches.shared.PATCH_NAME_CHANGE_LINK_SHARING_DOMAIN
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.returnEarly
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.logging.Logger

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/twitter/patches/links/ChangeLinkSharingDomainPatch;"

internal val domainNameOption by stringOption(
    key = "domainName",
    default = "https://fxtwitter.com",
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

internal val changeLinkSharingDomainResourcePatch = resourcePatch {
    execute {
        val domainName = domainNameOption!!

        val shareLinkTemplate = if (domainName.endsWith("/")) {
            "$domainName%1\$s/status/%2\$s"
        } else {
            "$domainName/%1\$s/status/%2\$s"
        }

        document("res/values/strings.xml").use { document ->
            document.documentElement.childNodes.findElementByAttributeValueOrThrow(
                "name",
                "tweet_share_link"
            ).textContent = shareLinkTemplate
        }
    }
}
@Suppress("unused")
val changeLinkSharingDomainPatch = bytecodePatch(
    name = PATCH_NAME_CHANGE_LINK_SHARING_DOMAIN,
    description = PATCH_DESCRIPTION_CHANGE_LINK_SHARING_DOMAIN
) {
    dependsOn(
        changeLinkSharingDomainResourcePatch,
        sharedExtensionPatch,
    )

    compatibleWith(
        "com.twitter.android"(
            "10.60.0-release.0",
            "10.86.0-release.0",
        )
    )

    execute {
        val domainName = domainNameOption!!

        // Replace the domain name in the link sharing extension methods.
        linkSharingDomainHelperFingerprint.method.returnEarly(domainName)

        // Replace the domain name when copying a link with "Copy link" button.
        linkBuilderFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p0, p1, p2 }, $EXTENSION_CLASS_DESCRIPTOR->formatLink(JLjava/lang/String;)Ljava/lang/String;
                move-result-object p0
                return-object p0
            """
        )
    }
}
