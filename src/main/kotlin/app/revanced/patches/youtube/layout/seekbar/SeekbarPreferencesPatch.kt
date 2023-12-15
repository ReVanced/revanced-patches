package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import java.io.Closeable

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class])
internal object SeekbarPreferencesPatch : ResourcePatch(), Closeable {
    private val seekbarPreferences = mutableListOf<BasePreference>()

    override fun execute(context: ResourceContext) {
        // Nothing to do here. All work is done in close method.
    }

    override fun close() {
        StringsPatch.includePatchStrings("SeekbarPreferences")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_seekbar_preference_screen",
                "revanced_seekbar_preference_screen_title",
                seekbarPreferences,
                "revanced_seekbar_preference_screen_summary"
            )
        )
    }

    internal fun addPreferences(vararg preferencesToAdd: BasePreference) =
        seekbarPreferences.addAll(preferencesToAdd)
}
