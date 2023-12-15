package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.AbstractSettingsResourcePatch
import app.revanced.patches.shared.settings.preference.addPreference
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch.HOOKED_SETTINGS_ACTIVITY_NAME
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import org.w3c.dom.Element
import org.w3c.dom.Node

@Patch(
    dependencies = [StringsPatch::class, ResourceMappingPatch::class]
)
object SettingsResourcePatch : AbstractSettingsResourcePatch(
    "revanced_prefs",
    "youtube/settings"
) {
    // Used for a fingerprint from SettingsPatch.
    internal var appearanceStringId = -1L

    // if this is not null, all intents will be renamed to this
    internal var overrideIntentsTargetPackage: String? = null

    private var preferencesNode: Node? = null

    private var preferencesEditor: DomFileEditor? = null
        set(value) {
            field = value
            preferencesNode = value.getNode("PreferenceScreen")
        }

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
            ResourceGroup("layout", "revanced_settings_with_toolbar.xml")
        ).forEach { resourceGroup ->
            context.copyResources("youtube/settings", resourceGroup)
        }

        preferencesEditor = context.xmlEditor["res/xml/settings_fragment.xml"]

        // Modify the manifest and add an data intent filter to the LicenseActivity.
        // Some devices freak out if undeclared data is passed to an intent,
        // and this change appears to fix the issue.
        var modifiedIntentFilter = false
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
                            && applicationChild.getAttribute("android:name") == HOOKED_SETTINGS_ACTIVITY_NAME
                        ) {
                            val intentFilter = editor.file.createElement("intent-filter")
                            val mimeType = editor.file.createElement("data")
                            mimeType.setAttribute("android:mimeType", "text/plain")
                            intentFilter.appendChild(mimeType)
                            applicationChild.appendChild(intentFilter)
                            modifiedIntentFilter = true
                            break
                        }
                    }
                }
            }
        }
        if (!modifiedIntentFilter) throw PatchException("Could not modify intent filter")

        // Add the ReVanced settings to the YouTube settings
        StringsPatch.includePatchStrings("Settings")

        SettingsPatch.addPreference(
            Preference(
                "revanced_settings_title",
                "revanced_settings_summary",
                SettingsPatch.createReVancedSettingsIntent("revanced_settings_intent")
            )
        )

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = null,
                titleKey = "revanced_pref_import_export_title",
                summaryKey = "revanced_pref_import_export_summary",
                inputType = InputType.TEXT_MULTI_LINE,
                tag = "app.revanced.integrations.settingsmenu.ImportExportPreference"
            )
        )
    }

    /**
     * Add a preference fragment to the main preferences.
     *
     * @param preference The preference to add.
     */
    fun addPreference(preference: Preference) = preferencesNode!!.addPreference(preference)

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