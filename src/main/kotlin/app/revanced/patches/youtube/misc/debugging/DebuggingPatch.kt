package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Enable debugging",
    description = "Adds options for debugging.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")],
)
@Suppress("unused")
object DebuggingPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            PreferenceScreen(
                key = "revanced_debug_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_debug"),
                    SwitchPreference("revanced_debug_protobuffer"),
                    SwitchPreference("revanced_debug_stacktrace"),
                    SwitchPreference("revanced_debug_toast_on_error"),
                ),
            ),
        )
    }
}
