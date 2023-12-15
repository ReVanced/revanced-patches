package app.revanced.patches.shared.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.addPreference
import app.revanced.patches.shared.settings.preference.addResource
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import org.w3c.dom.Node
import java.io.Closeable

/**
 * Abstract settings resource patch
 *
 * @param preferenceFileName Name of the settings preference xml file
 * @param sourceDirectory Source directory to copy the preference template from
 */
abstract class AbstractSettingsResourcePatch(
    private val preferenceFileName: String,
    private val sourceDirectory: String,
) : ResourcePatch(), Closeable {

    private lateinit var arraysEditor: DomFileEditor
    private lateinit var revancedPreferencesEditor: DomFileEditor

    override fun execute(context: ResourceContext) {
        /* copy preference template from source dir */
        context.copyResources(
            sourceDirectory,
            ResourceGroup(
                "xml", "$preferenceFileName.xml"
            )
        )

        /* prepare xml editors */
        arraysEditor = context.xmlEditor["res/values/arrays.xml"]
        arraysNode = arraysEditor.getNode("resources")

        revancedPreferencesEditor = context.xmlEditor["res/xml/$preferenceFileName.xml"]
        revancedPreferenceNode = revancedPreferencesEditor.getNode("PreferenceScreen")
    }

    override fun close() {
        revancedPreferencesEditor.close()
        arraysEditor.close()
    }

    internal companion object {
        private lateinit var arraysNode: Node
        private lateinit var revancedPreferenceNode: Node

        /**
         * Add an array to the resources.
         *
         * @param arrayResource The array resource to add.
         */
        internal fun addArray(arrayResource: ArrayResource) = arraysNode.addResource(arrayResource)

        /**
         * Add a preference to the settings.
         *
         * @param preference The preference to add.
         */
        fun addPreference(preference: BasePreference) = revancedPreferenceNode.addPreference(preference)

        internal fun DomFileEditor?.getNode(tagName: String) =
            this!!.file.getElementsByTagName(tagName).item(0)
    }
}