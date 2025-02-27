package app.revanced.patches.protonmail.footer

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import app.revanced.util.findElementByAttributeValueOrThrow

@Suppress("unused")
val removeMobileFooterPatch = resourcePatch(
    name = "Remove 'sent from' signature",
    description = "Removes the 'Sent from Proton Mail mobile' signature from emails.",
) {
    execute {
        document("res/values/strings.xml").use { document ->
            document.documentElement.childNodes.findElementByAttributeValueOrThrow("name", "mail_settings_identity_mobile_footer_default_free").textContent = ""
        }
    }
}