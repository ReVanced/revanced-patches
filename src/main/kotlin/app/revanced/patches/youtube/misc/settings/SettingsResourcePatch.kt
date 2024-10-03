package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.BaseSettingsResourcePatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.copyXmlNode
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.inputStreamFromBundledResource

object SettingsResourcePatch : BaseSettingsResourcePatch(
    IntentPreference(
        titleKey = "revanced_settings_title",
        summaryKey = null,
        intent = SettingsPatch.newIntent("revanced_settings_intent"),
    ) to "settings_fragment",
    dependencies =
    setOf(
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    ),
) {
    // Used for a fingerprint from SettingsPatch.
    internal var appearanceStringId = -1L

    override fun execute(context: ResourceContext) {
        super.execute(context)

        // Used for a fingerprint from SettingsPatch.
        appearanceStringId = ResourceMappingPatch["string", "app_theme_appearance_dark"]

        arrayOf(
            ResourceGroup("layout", "revanced_settings_with_toolbar.xml"),
        ).forEach { resourceGroup ->
            context.copyResources("settings", resourceGroup)
        }

        // Copy style properties used to fix over-sized copy menu that appear in EditTextPreference.
        // For a full explanation of how this fixes the issue, see the comments in this style file
        // and the comments in the integrations code.
        val targetResource = "values/styles.xml"
        inputStreamFromBundledResource(
            "settings/host",
            targetResource
        )!!.let { inputStream ->
            "resources".copyXmlNode(
                context.xmlEditor[inputStream],
                context.xmlEditor["res/${targetResource}"]
            ).close()
        }

        // Remove horizontal divider from the settings Preferences
        // To better match the appearance of the stock YouTube settings.
        context.xmlEditor["res/values/styles.xml"].use { editor ->
            val document = editor.file

            arrayOf(
                "Theme.YouTube.Settings",
                "Theme.YouTube.Settings.Dark"
            ).forEach { value ->
                val listDividerNode = document.createElement("item")
                listDividerNode.setAttribute("name", "android:listDivider")
                listDividerNode.appendChild(document.createTextNode("@null"))

                document.childNodes.findElementByAttributeValueOrThrow(
                    "name", value
                ).appendChild(listDividerNode)
            }
        }

        // Modify the manifest and add a data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file

            val licenseElement = document.childNodes.findElementByAttributeValueOrThrow(
                "android:name",
                "com.google.android.libraries.social.licenses.LicenseActivity"
            )

            val mimeType = document.createElement("data")
            mimeType.setAttribute("android:mimeType", "text/plain")

            val intentFilter = document.createElement("intent-filter")
            intentFilter.appendChild(mimeType)

            licenseElement.appendChild(intentFilter)
        }
    }
}
