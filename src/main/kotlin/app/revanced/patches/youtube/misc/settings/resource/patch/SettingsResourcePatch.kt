package app.revanced.patches.youtube.misc.settings.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.DomFileEditor
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.addPreference
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.resource.patch.AbstractSettingsResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import app.revanced.util.resources.ResourceUtils.mergeStrings
import org.w3c.dom.Node

@Name("settings-resource-patch")
@DependsOn([ResourceMappingPatch::class])
@Description("Applies mandatory patches to implement ReVanced settings into the application.")
@Version("0.0.1")
class SettingsResourcePatch : AbstractSettingsResourcePatch(
    "revanced_prefs",
    "settings"
) {
    override fun execute(context: ResourceContext): PatchResult {
        super.execute(context)

        /*
         * used by a fingerprint of SettingsPatch
         */
        appearanceStringId = ResourceMappingPatch.resourceMappings.find {
            it.type == "string" && it.name == "app_theme_appearance_dark"
        }!!.id

        /*
         * create missing directory for the resources
         */
        context["res/drawable-ldrtl-xxxhdpi"].mkdirs()

        /*
         * copy layout resources
         */
        arrayOf(
            ResourceUtils.ResourceGroup(
                "layout",
                "revanced_settings_toolbar.xml",
                "revanced_settings_with_toolbar.xml",
                "revanced_settings_with_toolbar_layout.xml"
            ), ResourceUtils.ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable-xxxhdpi", "quantum_ic_arrow_back_white_24.png"
            ), ResourceUtils.ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable-ldrtl-xxxhdpi", "quantum_ic_arrow_back_white_24.png"
            )
        ).forEach { resourceGroup ->
            context.copyResources("settings", resourceGroup)
        }

        preferencesEditor = context.xmlEditor["res/xml/settings_fragment.xml"]

        // Add the ReVanced settings to the YouTube settings
        val youtubePackage = "com.google.android.youtube"
        SettingsPatch.addPreference(
            Preference(
                StringResource("revanced_settings", "ReVanced"),
                Preference.Intent(
                    youtubePackage, "revanced_settings", "com.google.android.libraries.social.licenses.LicenseActivity"
                ),
                StringResource("revanced_settings_summary", "ReVanced specific settings"),
            )
        )

        context.mergeStrings("settings/host/values/strings.xml")

        return PatchResultSuccess()
    }


    internal companion object {
        // Used by a fingerprint of SettingsPatch
        // this field is located in the SettingsResourcePatch
        // because if it were to be defined in the SettingsPatch companion object,
        // the companion object could be initialized before ResourceMappingResourcePatch has executed.
        internal var appearanceStringId: Long = -1

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