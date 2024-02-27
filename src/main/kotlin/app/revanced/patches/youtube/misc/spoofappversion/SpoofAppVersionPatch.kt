package app.revanced.patches.youtube.misc.spoofappversion

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.versionspoof.AbstractVersionSpoofPatch
import app.revanced.patches.youtube.utils.integrations.Constants.MISC_PATH
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch.contexts
import app.revanced.util.copyXmlNode
import org.w3c.dom.Element

@Patch(
    name = "Spoof app version",
    description = "Adds options to spoof the YouTube client version. " +
            "This can be used to restore old UI elements and features.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object SpoofAppVersionPatch : AbstractVersionSpoofPatch(
    "$MISC_PATH/SpoofAppVersionPatch;->getVersionOverride(Ljava/lang/String;)Ljava/lang/String;"
) {
    override fun execute(context: BytecodeContext) {
        super.execute(context)

        /**
         * Copy arrays
         */
        contexts.copyXmlNode("youtube/spoofappversion/host", "values/arrays.xml", "resources")

        if (SettingsPatch.upward1834) {
            contexts.appendChild(
                arrayOf(
                    "revanced_spoof_app_version_target_entry" to "@string/revanced_spoof_app_version_target_entry_18_33_40",
                    "revanced_spoof_app_version_target_entry_value" to "18.33.40",
                )
            )

            if (SettingsPatch.upward1839) {
                contexts.appendChild(
                    arrayOf(
                        "revanced_spoof_app_version_target_entry" to "@string/revanced_spoof_app_version_target_entry_18_38_45",
                        "revanced_spoof_app_version_target_entry_value" to "18.38.45"
                    )
                )

                if (SettingsPatch.upward1841) {
                    contexts.appendChild(
                        arrayOf(
                            "revanced_spoof_app_version_target_entry" to "@string/revanced_spoof_app_version_target_entry_18_40_34",
                            "revanced_spoof_app_version_target_entry_value" to "18.40.34"
                        )
                    )

                    if (SettingsPatch.upward1843) {
                        contexts.appendChild(
                            arrayOf(
                                "revanced_spoof_app_version_target_entry" to "@string/revanced_spoof_app_version_target_entry_18_42_41",
                                "revanced_spoof_app_version_target_entry_value" to "18.42.41"
                            )
                        )
                    }
                }
            }
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: EXPERIMENTAL_FLAGS",
                "SETTINGS: SPOOF_APP_VERSION"
            )
        )

        SettingsPatch.updatePatchStatus("Spoof app version")

    }

    private fun ResourceContext.appendChild(entryArray: Array<Pair<String, String>>) {
        entryArray.map { (attributeName, attributeValue) ->
            this.xmlEditor["res/values/arrays.xml"].use { editor ->
                editor.file.apply {
                    val resourcesNode = getElementsByTagName("resources").item(0) as Element

                    val newElement: Element = createElement("item")
                    for (i in 0 until resourcesNode.childNodes.length) {
                        val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                        if (node.getAttribute("name") == attributeName) {
                            newElement.appendChild(createTextNode(attributeValue))
                            val firstChild = node.firstChild

                            node.insertBefore(newElement, firstChild)
                        }
                    }
                }
            }
        }
    }
}