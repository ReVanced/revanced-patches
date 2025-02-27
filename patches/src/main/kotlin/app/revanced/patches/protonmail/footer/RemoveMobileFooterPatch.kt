package app.revanced.patches.protonmail.footer

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val removeMobileFooterPatch = resourcePatch(
    name = "Remove 'sent from' signature",
    description = "Removes the 'Sent from Proton Mail mobile' signature from emails.",
) {
    execute {
        document("res/values/strings.xml").use { document ->
            val stringElements = document.getElementsByTagName("string")
            val element = (0 until stringElements.length)
                .mapNotNull { stringElements.item(it) as? Element }
                .find { it.getAttribute("name") == "mail_settings_identity_mobile_footer_default_free" }

            element?.textContent = ""
        }
    }
}