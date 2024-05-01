package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.baseSettingsResourcePatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.asSequence
import app.revanced.util.copyResources
import org.w3c.dom.Element

// Used for a fingerprint from SettingsPatch.
internal var appearanceStringId = -1L

val settingsResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
        baseSettingsResourcePatch(
            IntentPreference(
                titleKey = "revanced_settings_title",
                summaryKey = null,
                intent = newIntent("revanced_settings_intent"),
            ) to "settings_fragment",
        ),
    )

    execute { context ->
        // Used for a fingerprint from SettingsPatch.
        appearanceStringId = resourceMappings["string", "app_theme_appearance_dark"]

        arrayOf(
            ResourceGroup("layout", "revanced_settings_with_toolbar.xml"),
        ).forEach { resourceGroup ->
            context.copyResources("settings", resourceGroup)
        }

        // Remove horizontal divider from the settings Preferences
        // To better match the appearance of the stock YouTube settings.
        context.document["res/values/styles.xml"].use { document ->
            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            resourcesNode.childNodes.asSequence().forEach {
                val node = it as? Element ?: return@forEach

                val name = node.getAttribute("name")
                if (name == "Theme.YouTube.Settings" || name == "Theme.YouTube.Settings.Dark") {
                    val listDividerNode = document.createElement("item")
                    listDividerNode.setAttribute("name", "android:listDivider")
                    listDividerNode.appendChild(document.createTextNode("@null"))
                    node.appendChild(listDividerNode)
                }
            }
        }

        // Modify the manifest and add a data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        var modifiedIntent = false
        context.document["AndroidManifest.xml"].use { document ->
            // A xml regular-expression would probably work better than this manual searching.
            val manifestNodes = document.getElementsByTagName("manifest").item(0).childNodes
            for (i in 0..manifestNodes.length) {
                val node = manifestNodes.item(i)
                if (node != null && node.nodeName == "application") {
                    val applicationNodes = node.childNodes
                    for (j in 0..applicationNodes.length) {
                        val applicationChild = applicationNodes.item(j)
                        if (applicationChild is Element && applicationChild.nodeName == "activity" &&
                            applicationChild.getAttribute("android:name") ==
                            "com.google.android.libraries.social.licenses.LicenseActivity"
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
