package app.revanced.patches.youtube.layout.seekbar.resource

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import java.io.Closeable

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
class SeekbarPreferencesPatch : ResourcePatch, Closeable {
    override fun execute(context: ResourceContext) {

        // Nothing to do here. All work is done in close method.
    }

    override fun close() {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_seekbar_preference_screen",
                StringResource("revanced_seekbar_preference_screen_title", "Seekbar settings"),
                seekbarPreferences
            )
        )
    }

    companion object {
        private val seekbarPreferences = mutableListOf<BasePreference>()

        internal fun addPreferences(vararg preferencesToAdd: BasePreference) {
            seekbarPreferences.addAll(preferencesToAdd)
        }
    }
}
