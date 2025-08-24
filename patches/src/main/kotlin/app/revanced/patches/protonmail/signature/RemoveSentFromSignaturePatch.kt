package app.revanced.patches.protonmail.signature

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findElementByAttributeValue
import java.io.File

@Suppress("unused")
val removeSentFromSignaturePatch = resourcePatch(
    name = "Remove 'Sent from' signature",
    description = "Removes the 'Sent from Proton Mail mobile' signature from emails.",
) {
    compatibleWith("ch.protonmail.android"("4.15.0"))

    execute {
        val stringResourceFiles = mutableListOf<File>()

        get("res").walk().forEach { file ->
            if (file.isFile && file.name.equals("strings.xml", ignoreCase = true)) {
                stringResourceFiles.add(file)
            }
        }

        var foundString = false
        stringResourceFiles.forEach { filePath ->
            document(filePath.absolutePath).use { document ->
                var node = document.documentElement.childNodes.findElementByAttributeValue(
                    "name",
                    "mail_settings_identity_mobile_footer_default_free"
                )

                // String is not localized in all languages.
                if (node != null) {
                    node.textContent = ""
                    foundString = true
                }
            }
        }

        if (!foundString) throw PatchException("Could not find 'sent from' string in resources")
    }
}
