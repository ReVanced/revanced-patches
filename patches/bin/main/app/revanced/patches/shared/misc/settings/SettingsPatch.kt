package app.revanced.patches.shared.misc.settings

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResource
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.getNode
import app.revanced.util.insertFirst
import org.w3c.dom.Node

// TODO: Delete this on next major version bump.
@Deprecated("Use non deprecated settings patch function")
fun settingsPatch (
    rootPreference: Pair<IntentPreference, String>,
    preferences: Set<BasePreference>,
) = settingsPatch(listOf(rootPreference), preferences)

/**
 * A resource patch that adds settings to a settings fragment.
 *
 * @param rootPreferences List of intent preferences and the name of the fragment file to add it to.
 *                        File names that do not exist are ignored and not processed.
 * @param preferences A set of preferences to add to the ReVanced fragment.
 */
fun settingsPatch (
    rootPreferences: List<Pair<BasePreference, String>>? = null,
    preferences: Set<BasePreference>,
) = resourcePatch {
    dependsOn(addResourcesPatch)

    execute {
        copyResources(
            "settings",
            ResourceGroup("xml", "revanced_prefs.xml"),
        )

        addResources("shared", "misc.settings.settingsResourcePatch")
    }

    finalize {
        fun Node.addPreference(preference: BasePreference, prepend: Boolean = false) {
            preference.serialize(ownerDocument) { resource ->
                // TODO: Currently, resources can only be added to "values", which may not be the correct place.
                //  It may be necessary to ask for the desired resourceValue in the future.
                addResource("values", resource)
            }.let { preferenceNode ->
                insertFirst(preferenceNode)
            }
        }

        // Add the root preference to an existing fragment if needed.
        rootPreferences?.let {
            var modified = false

            it.forEach { (intent, fileName) ->
                val preferenceFileName = "res/xml/$fileName.xml"
                if (get(preferenceFileName).exists()) {
                    document(preferenceFileName).use { document ->
                        document.getNode("PreferenceScreen").addPreference(intent, true)
                    }
                    modified = true
                }
            }

            if (!modified) throw PatchException("No declared preference files exists: $rootPreferences")
        }

        // Add all preferences to the ReVanced fragment.
        document("res/xml/revanced_prefs.xml").use { document ->
            val revancedPreferenceScreenNode = document.getNode("PreferenceScreen")
            preferences.forEach { revancedPreferenceScreenNode.addPreference(it) }
        }
    }
}
