package app.revanced.patches.protonmail.signature

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import app.revanced.util.findElementByAttributeValueOrThrow

@Suppress("unused")
val removeSentFromSignaturePatch = resourcePatch(
    name = "Remove \"Sent from\" signature",
    description = "Removes the 'Sent from Proton Mail mobile' signature from emails.",
) {
    compatibleWith("ch.protonmail.android"("4.7.2"))
    execute {
        //TODO: This goes out of date if Proton Mail adds additional languages.
        //It could be improved by dynamically iterating through all directories in /res/ .
        val directories = listOf(
            "res/values",
            "res/values-b+es+419",
            "res/values-be",
            "res/values-ca",
            "res/values-cs",
            "res/values-da",
            "res/values-de",
            "res/values-el",
            "res/values-es-rES",
            "res/values-fr",
            "res/values-hr",
            "res/values-hu",
            "res/values-in",
            "res/values-it",
            "res/values-ja",
            "res/values-ka",
            "res/values-kab",
            "res/values-ko",
            "res/values-nb-rNO",
            "res/values-nl",
            "res/values-pl",
            "res/values-pt-rBR",
            "res/values-pt-rPT",
            "res/values-ro",
            "res/values-ru",
            "res/values-sk",
            "res/values-sl",
            "res/values-sv-rSE",
            "res/values-tr",
            "res/values-uk",
            "res/values-zh-rCN",
            "res/values-zh-rTW",
        )
        directories.forEach { directory ->
            document("$directory/strings.xml").use { document ->
                document.documentElement.childNodes.findElementByAttributeValueOrThrow("name", "mail_settings_identity_mobile_footer_default_free").textContent = ""
            }
        }
    }
}