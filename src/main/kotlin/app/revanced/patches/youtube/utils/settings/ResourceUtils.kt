package app.revanced.patches.youtube.utils.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.util.doRecursively
import app.revanced.util.insertNode
import org.w3c.dom.Element

@Suppress("MemberVisibilityCanBePrivate")
object ResourceUtils {

    const val TARGET_PREFERENCE_PATH = "res/xml/revanced_prefs.xml"

    const val YOUTUBE_SETTINGS_PATH = "res/xml/settings_fragment.xml"

    var targetPackage = "com.google.android.youtube"

    fun setMicroG(newPackage: String) {
        targetPackage = newPackage
    }

    fun ResourceContext.addEntryValues(
        path: String,
        speedEntryValues: String,
        attributeName: String
    ) {
        xmlEditor[path].use {
            with(it.file) {
                val resourcesNode = getElementsByTagName("resources").item(0) as Element

                val newElement: Element = createElement("item")

                for (i in 0 until resourcesNode.childNodes.length) {
                    val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                    if (node.getAttribute("name") == attributeName) {
                        newElement.appendChild(createTextNode(speedEntryValues))

                        node.appendChild(newElement)
                    }
                }
            }
        }
    }

    fun ResourceContext.addPreference(settingArray: Array<String>) {
        val prefs = this[TARGET_PREFERENCE_PATH]

        settingArray.forEach preferenceLoop@{ preference ->
            prefs.writeText(
                prefs.readText()
                    .replace("<!-- $preference", "")
                    .replace("$preference -->", "")
            )
        }
    }

    fun ResourceContext.updatePatchStatus(patchTitle: String) {
        updatePatchStatusSettings(patchTitle, "@string/revanced_patches_included")
    }

    fun ResourceContext.updatePatchStatusHeader(headerName: String) {
        updatePatchStatusSettings("Header", headerName)
    }

    fun ResourceContext.updatePatchStatusIcon(iconName: String) {
        updatePatchStatusSettings("Icon", "@string/revanced_icon_$iconName")
    }

    fun ResourceContext.updatePatchStatusLabel(appName: String) {
        updatePatchStatusSettings("Label", appName)
    }

    fun ResourceContext.updatePatchStatusTheme(themeName: String) {
        updatePatchStatusSettings("Theme", themeName)
    }

    fun ResourceContext.updatePatchStatusSettings(
        patchTitle: String,
        updateText: String
    ) {
        this.xmlEditor[TARGET_PREFERENCE_PATH].use { editor ->
            editor.file.doRecursively loop@{
                if (it !is Element) return@loop

                it.getAttributeNode("android:title")?.let { attribute ->
                    if (attribute.textContent == patchTitle) {
                        it.getAttributeNode("android:summary").textContent = updateText
                    }
                }
            }
        }
    }

    fun ResourceContext.addReVancedPreference(key: String) {
        val targetClass =
            "com.google.android.apps.youtube.app.settings.videoquality.VideoQualitySettingsActivity"

        this.xmlEditor[YOUTUBE_SETTINGS_PATH].use { editor ->
            with(editor.file) {
                doRecursively loop@{
                    if (it !is Element) return@loop
                    it.getAttributeNode("android:key")?.let { attribute ->
                        if (attribute.textContent == "@string/about_key" && it.getAttributeNode("app:iconSpaceReserved").textContent == "false") {
                            it.insertNode("Preference", it) {
                                setAttribute("android:title", "@string/revanced_" + key + "_title")
                                this.appendChild(
                                    ownerDocument.createElement("intent").also { intentNode ->
                                        intentNode.setAttribute(
                                            "android:targetPackage",
                                            targetPackage
                                        )
                                        intentNode.setAttribute("android:data", key)
                                        intentNode.setAttribute("android:targetClass", targetClass)
                                    })
                            }
                            it.getAttributeNode("app:iconSpaceReserved").textContent = "true"
                            return@loop
                        }
                    }
                }

                doRecursively loop@{
                    if (it !is Element) return@loop

                    it.getAttributeNode("app:iconSpaceReserved")?.let { attribute ->
                        if (attribute.textContent == "true") {
                            attribute.textContent = "false"
                        }
                    }
                }
            }
        }
    }
}