package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Enable debugging",
    description = "Adds debugging options.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")]
)
@Suppress("unused")
object DebuggingPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("Debugging")
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            app.revanced.patches.shared.settings.preference.impl.PreferenceScreen(
                "revanced_debug_preference_screen",
                "revanced_debug_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_debug",
                        "revanced_debug_title",
                        "revanced_debug_summary_on",
                        "revanced_debug_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_debug_protobuffer",
                        "revanced_debug_protobuffer_title",
                        "revanced_debug_protobuffer_summary_on",
                        "revanced_debug_protobuffer_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_debug_stacktrace",
                        "revanced_debug_stacktrace_title",
                        "revanced_debug_stacktrace_summary_on",
                        "revanced_debug_stacktrace_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_debug_toast_on_error",
                        "revanced_debug_toast_on_error_title",
                        "revanced_debug_toast_on_error_summary_on",
                        "revanced_debug_toast_on_error_summary_off"
                    ),
                ),
                "revanced_debug_preference_screen_summary"
            )
        )
    }
}
