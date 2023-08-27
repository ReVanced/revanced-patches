package app.revanced.patches.youtube.misc.settings.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.addPreference
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.shared.settings.resource.patch.AbstractSettingsResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import app.revanced.util.resources.ResourceUtils.mergeStrings
import org.w3c.dom.Element
import org.w3c.dom.Node

@DependsOn([ResourceMappingPatch::class])
class SettingsResourcePatch : AbstractSettingsResourcePatch(
    "revanced_prefs",
    "settings"
) {
    override fun execute(context: ResourceContext) {
        super.execute(context)

        // Used for a fingerprint from SettingsPatch.
        appearanceStringId = ResourceMappingPatch.resourceMappings.find {
            it.type == "string" && it.name == "app_theme_appearance_dark"
        }!!.id

        /*
         * copy layout resources
         */
        arrayOf(
            ResourceUtils.ResourceGroup("layout", "revanced_settings_with_toolbar.xml")
        ).forEach { resourceGroup ->
            context.copyResources("settings", resourceGroup)
        }

        preferencesEditor = context.xmlEditor["res/xml/settings_fragment.xml"]

        // Modify the manifest and add an data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            // An xml regular expression would probably work better than this manual searching.
            val manifestNodes = editor.file.getElementsByTagName("manifest").item(0).childNodes
            for (i in 0..manifestNodes.length) {
                val node = manifestNodes.item(i)
                if (node != null && node.nodeName == "application") {
                    val applicationNodes = node.childNodes
                    for (j in 0..applicationNodes.length) {
                        val applicationChild = applicationNodes.item(j)
                        if (applicationChild is Element && applicationChild.nodeName == "activity"
                            && applicationChild.getAttribute("android:name") == "com.google.android.libraries.social.licenses.LicenseActivity"
                        ) {
                            val intentFilter = editor.file.createElement("intent-filter")
                            val mimeType = editor.file.createElement("data")
                            mimeType.setAttribute("android:mimeType", "text/plain")
                            intentFilter.appendChild(mimeType)
                            applicationChild.appendChild(intentFilter)
                            break
                        }
                    }
                }
            }
        }


        // Add the ReVanced settings to the YouTube settings
        SettingsPatch.addPreference(
            Preference(
                StringResource("revanced_settings", "ReVanced"),
                StringResource("revanced_settings_summary", "ReVanced specific settings"),
                SettingsPatch.createReVancedSettingsIntent("revanced_settings")
            )
        )

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = null,
                title = StringResource("revanced_pref_import_export_title", "Import / Export"),
                summary = StringResource("revanced_pref_import_export_summary", "Import / Export ReVanced settings"),
                inputType = InputType.TEXT_MULTI_LINE,
                tag = "app.revanced.integrations.settingsmenu.ImportExportPreference"
            )
        )

        context.mergeStrings("settings/host/values/strings.xml")
    }


    internal companion object {
        // Used for a fingerprint from SettingsPatch.
        internal var appearanceStringId = -1L

        // if this is not null, all intents will be renamed to this
        var overrideIntentsTargetPackage: String? = null

        private var preferencesNode: Node? = null

        private var preferencesEditor: DomFileEditor? = null
            set(value) {
                field = value
                preferencesNode = value.getNode("PreferenceScreen")
            }

        /* Companion delegates */

        /**
         * Add a preference fragment to the main preferences.
         *
         * @param preference The preference to add.
         */
        fun addPreference(preference: Preference) =
            preferencesNode!!.addPreference(preference) { it.include() }

        /**
         * Add a new string to the resources.
         *
         * @param identifier The key of the string.
         * @param value The value of the string.
         * @throws IllegalArgumentException if the string already exists.
         */
        fun addString(identifier: String, value: String, formatted: Boolean) =
            AbstractSettingsResourcePatch.addString(identifier, value, formatted)

        /**
         * Add an array to the resources.
         *
         * @param arrayResource The array resource to add.
         */
        fun addArray(arrayResource: ArrayResource) = AbstractSettingsResourcePatch.addArray(arrayResource)

        /**
         * Add a preference to the settings.
         *
         * @param preferenceScreen The name of the preference screen.
         */
        fun addPreferenceScreen(preferenceScreen: PreferenceScreen) = addPreference(preferenceScreen)
    }

    override fun close() {
        super.close()

        // rename the intent package names if it was set
        overrideIntentsTargetPackage?.let { packageName ->
            val preferences = preferencesEditor!!.getNode("PreferenceScreen").childNodes
            for (i in 1 until preferences.length) {
                val preferenceNode = preferences.item(i)
                // preferences have a child node with the intent tag, skip over every other node
                if (preferenceNode.childNodes.length == 0) continue

                val intentNode = preferenceNode.firstChild

                // if the node doesn't have a target package attribute, skip it
                val targetPackageAttribute = intentNode.attributes.getNamedItem("android:targetPackage") ?: continue

                // do not replace intent target package if the package name is not from YouTube
                val youtubePackage = "com.google.android.youtube"
                if (targetPackageAttribute.nodeValue != youtubePackage) continue

                // replace the target package name
                intentNode.attributes.setNamedItem(preferenceNode.ownerDocument.createAttribute("android:targetPackage")
                    .also { attribute ->
                        attribute.value = packageName
                    })
            }
        }

        preferencesEditor?.close()
    }
}