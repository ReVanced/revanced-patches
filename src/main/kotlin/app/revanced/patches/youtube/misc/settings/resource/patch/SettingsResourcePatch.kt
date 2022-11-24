package app.revanced.patches.youtube.misc.settings.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.DomFileEditor
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.shared.settings.IResource
import app.revanced.patches.shared.settings.addPreference
import app.revanced.patches.shared.settings.addResource
import app.revanced.patches.shared.settings.impl.ArrayResource
import app.revanced.patches.shared.settings.impl.Preference
import app.revanced.patches.shared.settings.impl.PreferenceScreen
import app.revanced.patches.shared.settings.impl.StringResource
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import org.w3c.dom.Node

@Name("settings-resource-patch")
@SettingsCompatibility
@DependsOn([FixLocaleConfigErrorPatch::class, ResourceMappingPatch::class])
@Version("0.0.1")
class SettingsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
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
                "xml", "revanced_prefs.xml" // template for new preferences
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

        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            editor.file.getElementsByTagName("manifest").item(0).also {
                it.appendChild(it.ownerDocument.createElement("uses-permission").also { element ->
                    element.setAttribute("android:name", "android.permission.SCHEDULE_EXACT_ALARM")
                })
            }
        }

        revancedPreferencesEditor = context.xmlEditor["res/xml/revanced_prefs.xml"]
        preferencesEditor = context.xmlEditor["res/xml/settings_fragment.xml"]

        stringsEditor = context.xmlEditor["res/values/strings.xml"]
        arraysEditor = context.xmlEditor["res/values/arrays.xml"]

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

        private var revancedPreferenceNode: Node? = null
        private var preferencesNode: Node? = null

        private var stringsNode: Node? = null
        private var arraysNode: Node? = null

        private var strings = mutableListOf<StringResource>()

        private var revancedPreferencesEditor: DomFileEditor? = null
            set(value) {
                field = value
                revancedPreferenceNode = value.getNode("PreferenceScreen")
            }
        private var preferencesEditor: DomFileEditor? = null
            set(value) {
                field = value
                preferencesNode = value.getNode("PreferenceScreen")
            }

        private var stringsEditor: DomFileEditor? = null
            set(value) {
                field = value
                stringsNode = value.getNode("resources")
            }
        private var arraysEditor: DomFileEditor? = null
            set(value) {
                field = value
                arraysNode = value.getNode("resources")
            }

        /**
         * Add a new string to the resources.
         *
         * @param identifier The key of the string.
         * @param value The value of the string.
         * @throws IllegalArgumentException if the string already exists.
         */
        fun addString(identifier: String, value: String, formatted: Boolean) =
            StringResource(identifier, value, formatted).include()

        /**
         * Add an array to the resources.
         *
         * @param arrayResource The array resource to add.
         */
        fun addArray(arrayResource: ArrayResource) {
            arraysNode!!.appendChild(arrayResource.serialize(arraysNode!!.ownerDocument) { it.include() })
        }

        /**
         * Add a preference screen to the settings.
         *
         * @param preferenceScreen The name of the preference screen.
         */
        fun addPreferenceScreen(preferenceScreen: PreferenceScreen) =
            revancedPreferenceNode!!.addPreference(preferenceScreen) { it.include() }

        /**
         * Add a preference fragment to the preferences.
         *
         * @param preference The preference to add.
         */
        fun addPreference(preference: Preference) {
            preferencesNode!!.addPreference(preference) { it.include() }
        }

        /**
         * Add a new resource to the resources.
         *
         * @throws IllegalArgumentException if the resource already exists.
         */
        private fun IResource.include() {
            when(this) {
                is StringResource -> {
                    if (strings.any { it.name == name }) return
                    strings.add(this)
                }
                is ArrayResource -> addArray(this)
                else -> throw NotImplementedError("Unsupported resource type")
            }
        }

        private fun DomFileEditor?.getNode(tagName: String) = this!!.file.getElementsByTagName(tagName).item(0)

        private fun Node?.createElement(tagName: String) = this!!.ownerDocument.createElement(tagName)
    }

    override fun close() {
        // merge all strings, skip duplicates
        strings.forEach {
            stringsNode!!.addResource(it)
        }

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

        revancedPreferencesEditor?.close()
        preferencesEditor?.close()
        stringsEditor?.close()
        arraysEditor?.close()
    }
}