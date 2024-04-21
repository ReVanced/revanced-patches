package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.BaseSettingsResourcePatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import org.w3c.dom.Element

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

        AddResourcesPatch(this::class)

        // Used for a fingerprint from SettingsPatch.
        appearanceStringId = ResourceMappingPatch["string", "app_theme_appearance_dark"]

        arrayOf(
            ResourceGroup("layout", "revanced_settings_with_toolbar.xml"),
        ).forEach { resourceGroup ->
            context.copyResources("settings", resourceGroup)
        }

        // Remove horizontal divider from the settings Preferences
        // To better match the appearance of the stock YouTube settings.
        context.xmlEditor["res/values/styles.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            for (i in 0 until resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as? Element ?: continue
                val name = node.getAttribute("name")
                if (name == "Theme.YouTube.Settings" || name == "Theme.YouTube.Settings.Dark") {
                    val listDividerNode = editor.file.createElement("item")
                    listDividerNode.setAttribute("name", "android:listDivider")
                    listDividerNode.appendChild(editor.file.createTextNode("@null"))
                    node.appendChild(listDividerNode)
                }
            }
        }

        // Modify the manifest and add a data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        var modifiedIntent = false
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file
            // A xml regular-expression would probably work better than this manual searching.
            val manifestNodes = document.getElementsByTagName("manifest").item(0).childNodes
            for (i in 0..manifestNodes.length) {
                val node = manifestNodes.item(i)
                if (node != null && node.nodeName == "application") {
                    val applicationNodes = node.childNodes
                    for (j in 0..applicationNodes.length) {
                        val applicationChild = applicationNodes.item(j)
                        if (applicationChild is Element && applicationChild.nodeName == "activity" &&
                            applicationChild.getAttribute("android:name") == "com.google.android.libraries.social.licenses.LicenseActivity"
                        ) {
                            val intentFilter = document.createElement("intent-filter")
                            val mimeType = document.createElement("data")
                            mimeType.setAttribute("android:mimeType", "text/plain")
                            intentFilter.appendChild(mimeType)
                            applicationChild.appendChild(intentFilter)
                            modifiedIntent = true
                            break
                        }
                    }
                }
            }
        }

        if (!modifiedIntent) throw PatchException("Could not modify activity intent")
    }
}
