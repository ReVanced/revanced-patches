package app.revanced.patches.shared.misc.settings

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
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
abstract class BaseSettingsResourcePatch(
    private val rootPreference: Pair<IntentPreference, String>? = null,
    dependencies: Set<PatchClass> = emptySet(),
) : ResourcePatch(
    dependencies = setOf(AddResourcesPatch::class) + dependencies,
),
    MutableSet<BasePreference> by mutableSetOf(),
    Closeable {
    private lateinit var context: ResourceContext

    override fun execute(context: ResourceContext) {
        context.copyResources(
            "settings",
            ResourceGroup("xml", "revanced_prefs.xml"),
        )

        this.context = context

        AddResourcesPatch(BaseSettingsResourcePatch::class)
    }

    override fun close() {
        fun Node.addPreference(preference: BasePreference, prepend: Boolean = false) {
            preference.serialize(ownerDocument) { resource ->
                // TODO: Currently, resources can only be added to "values", which may not be the correct place.
                //  It may be necessary to ask for the desired resourceValue in the future.
                AddResourcesPatch("values", resource)
            }.let { preferenceNode ->
                if (prepend && firstChild != null) {
                    insertBefore(preferenceNode, firstChild)
                } else {
                    appendChild(preferenceNode)
                }
            }
        }

        // Add the root preference to an existing fragment if needed.
        rootPreference?.let { (intentPreference, fragment) ->
            context.xmlEditor["res/xml/$fragment.xml"].use { editor ->
                val document = editor.file

                document.getNode("PreferenceScreen").addPreference(intentPreference, true)
            }
        }

        // Add all preferences to the ReVanced fragment.
        context.xmlEditor["res/xml/revanced_prefs.xml"].use { editor ->
            val document = editor.file

            val revancedPreferenceScreenNode = document.getNode("PreferenceScreen")
            forEach { revancedPreferenceScreenNode.addPreference(it) }
        }
    }
}
