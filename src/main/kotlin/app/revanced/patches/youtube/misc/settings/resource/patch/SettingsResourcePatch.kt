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
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.BasePreference
import app.revanced.patches.youtube.misc.settings.framework.components.impl.*
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import org.w3c.dom.Element
import org.w3c.dom.Node

@Name("settings-resource-patch")
@SettingsCompatibility
@DependsOn([FixLocaleConfigErrorPatch::class, ResourceMappingResourcePatch::class])
@Version("0.0.1")
class SettingsResourcePatch : ResourcePatch {

    override fun execute(context: ResourceContext): PatchResult {
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
            arraysNode!!.appendChild(arraysNode!!.ownerDocument.createElement("string-array").also { arrayNode ->
                arrayResource.items.forEach { item ->
                    item.include()

                    arrayNode.setAttribute("name", item.name)

                    arrayNode.appendChild(arrayNode.ownerDocument.createElement("item").also { itemNode ->
                        itemNode.textContent = item.value
                    })
                }
            })
        }

        /**
         * Add a preference screen to the settings.
         *
         * @param preferenceScreen The name of the preference screen.
         */
        fun addPreferenceScreen(preferenceScreen: PreferenceScreen) =
            revancedPreferenceNode!!.addPreference(preferenceScreen)

        /**
         * Add a preference fragment to the preferences.
         *
         * @param preference The preference to add.
         */
        fun addPreference(preference: Preference) {
            preferencesNode!!.appendChild(preferencesNode.createElement(preference.tag).also { preferenceNode ->
                preferenceNode.setAttribute(
                    "android:title", "@string/${preference.title.also { it.include() }.name}"
                )
                preference.summary?.let { summary ->
                    preferenceNode.setAttribute("android:summary", "@string/${summary.also { it.include() }.name}")
                }

                preferenceNode.appendChild(preferenceNode.createElement("intent").also { intentNode ->
                    intentNode.setAttribute("android:targetPackage", preference.intent.targetPackage)
                    intentNode.setAttribute("android:data", preference.intent.data)
                    intentNode.setAttribute("android:targetClass", preference.intent.targetClass)
                })
            })
        }


        /**
         * Add a preference to the settings.
         *
         * @param preference The preference to add.
         */
        private fun Node.addPreference(preference: BasePreference) {
            // add a summary to the element
            fun Element.addSummary(summaryResource: StringResource?, summaryType: SummaryType = SummaryType.DEFAULT) =
                summaryResource?.let { summary ->
                    setAttribute("android:${summaryType.type}", "@string/${summary.also { it.include() }.name}")
                }

            fun <T> Element.addDefault(default: T) {
                default?.let {
                    setAttribute(
                        "android:defaultValue", when (it) {
                            is Boolean -> if (it) "true" else "false"
                            is String -> it
                            else -> throw IllegalArgumentException("Unsupported default value type: ${it::class.java.name}")
                        }
                    )
                }
            }

            val preferenceElement = ownerDocument.createElement(preference.tag)
            preferenceElement.setAttribute("android:key", preference.key)
            preferenceElement.setAttribute("android:title", "@string/${preference.title.also { it.include() }.name}")

            when (preference) {
                is PreferenceScreen -> {
                    for (childPreference in preference.preferences) preferenceElement.addPreference(childPreference)
                    preferenceElement.addSummary(preference.summary)
                }
                is SwitchPreference -> {
                    preferenceElement.addDefault(preference.default)
                    preferenceElement.addSummary(preference.summaryOn, SummaryType.ON)
                    preferenceElement.addSummary(preference.summaryOff, SummaryType.OFF)
                }
                is TextPreference -> {
                    preferenceElement.setAttribute("android:inputType", preference.inputType.type)
                    preferenceElement.addDefault(preference.default)
                    preferenceElement.addSummary(preference.summary)
                }
            }

            appendChild(preferenceElement)
        }

        /**
         * Add a new string to the resources.
         *
         * @throws IllegalArgumentException if the string already exists.
         */
        private fun StringResource.include() {
            if (strings.any { it.name == name }) return
            strings.add(this)
        }

        private fun DomFileEditor?.getNode(tagName: String) = this!!.file.getElementsByTagName(tagName).item(0)

        private fun Node?.createElement(tagName: String) = this!!.ownerDocument.createElement(tagName)

        private enum class SummaryType(val type: String) {
            DEFAULT("summary"), ON("summaryOn"), OFF("summaryOff")
        }
    }

    override fun close() {
        // merge all strings, skip duplicates
        strings.forEach { stringResource ->
            stringsNode!!.appendChild(stringsNode!!.ownerDocument.createElement("string").also { stringElement ->
                stringElement.setAttribute("name", stringResource.name)

                // if the string is un-formatted, explicitly add the formatted attribute
                if (!stringResource.formatted) stringElement.setAttribute("formatted", "false")

                stringElement.textContent = stringResource.value
            })
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