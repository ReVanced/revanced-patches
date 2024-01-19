package app.revanced.patches.shared.settings

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.impl.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.getNode
import org.w3c.dom.Node
import java.io.Closeable

/**
 * A resource patch that adds settings to a settings fragment.
 *
 * @param rootPreference A pair of an intent preference and the name of the fragment file to add it to.
 * If null, no preference will be added.
 * @param dependencies Additional dependencies of this patch.
 */
abstract class AbstractSettingsResourcePatch(
    private val rootPreference: Pair<IntentPreference, String>? = null,
    dependencies: Set<PatchClass> = emptySet()
) : ResourcePatch(
    dependencies = setOf(AddResourcesPatch::class) + dependencies
), Closeable {
    private lateinit var revancedPreferencesEditor: DomFileEditor
    private lateinit var revancedPreferenceScreenNode: Node

    override fun execute(context: ResourceContext) {
        context.copyResources(
            "settings",
            ResourceGroup("xml", "revanced_prefs.xml")
        )

        rootPreference?.let { (intentPreference, fragment) ->
            context.xmlEditor["res/xml/$fragment.xml"].use {
                it.getNode("PreferenceScreen").addPreference(intentPreference)
            }
        }

        revancedPreferencesEditor = context.xmlEditor["res/xml/revanced_prefs.xml"]
        revancedPreferenceScreenNode = revancedPreferencesEditor.getNode("PreferenceScreen")

        AddResourcesPatch(AbstractSettingsResourcePatch::class)
    }

    /**
     * Add a preference to the settings fragment file.
     *
     * @param preference The preference to add.
     */
    fun addPreference(preference: BasePreference) {
        revancedPreferenceScreenNode.addPreference(preference)
    }

    private fun Node.addPreference(preference: BasePreference) {
        preference.serialize(ownerDocument) { resource ->
            AddResourcesPatch += resource
        }.let(this::appendChild)
    }

    override fun close() {
        revancedPreferencesEditor.close()
    }
}
