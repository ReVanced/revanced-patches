package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import java.io.Closeable

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class, AddResourcesPatch::class])
internal object SeekbarPreferencesPatch : ResourcePatch(), Closeable {
    private val seekbarPreferences = mutableSetOf<BasePreference>()

    override fun execute(context: ResourceContext) {
        // Nothing to do here. All work is done in close method.
    }

    override fun close() {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_seekbar_preference_screen",
                preferences = seekbarPreferences,
            )
        )
    }

    internal fun addPreferences(vararg preferencesToAdd: BasePreference) =
        seekbarPreferences.addAll(preferencesToAdd)
}
