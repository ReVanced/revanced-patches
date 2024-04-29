package app.revanced.patches.shared.misc.settings

import app.revanced.patcher.patch.*
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.getNode
import org.w3c.dom.Node

val preferences = mutableSetOf<BasePreference>()

/**
 * A resource patch that adds settings to a settings fragment.
 *
 * @param rootPreference A pair of an intent preference and the name of the fragment file to add it to.
 * If null, no preference will be added.
 */
fun baseSettingsResourcePatch(
    rootPreference: Pair<IntentPreference, String>? = null,
) = resourcePatch {
    dependsOn(addResourcesPatch)

    execute { context ->
        context.copyResources(
            "settings",
            ResourceGroup("xml", "revanced_prefs.xml"),
        )

        addResources("shared", "misc.settings.BaseSettingsResourcePatch")
    }

    finalize { context ->
        fun Node.addPreference(preference: BasePreference, prepend: Boolean = false) {
            preference.serialize(ownerDocument) { resource ->
                // TODO: Currently, resources can only be added to "values", which may not be the correct place.
                //  It may be necessary to ask for the desired resourceValue in the future.
                addResources("values", resource)
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
            context.document["res/xml/$fragment.xml"].use { document ->
                document.getNode("PreferenceScreen").addPreference(intentPreference, true)
            }
        }

        // Add all preferences to the ReVanced fragment.
        context.document["res/xml/revanced_prefs.xml"].use { document ->
            val revancedPreferenceScreenNode = document.getNode("PreferenceScreen")
            preferences.forEach { revancedPreferenceScreenNode.addPreference(it) }
        }
    }
}
