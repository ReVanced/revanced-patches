package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
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
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            app.revanced.patches.shared.settings.preference.impl.PreferenceScreen(
                "revanced_debug_preference_screen",
                StringResource("revanced_debug_preference_screen_title", "Debugging"),
                listOf(
                    SwitchPreference(
                        "revanced_debug",
                        StringResource("revanced_debug_title", "Debug logging"),
                        StringResource("revanced_debug_summary_on", "Debug logs are enabled"),
                        StringResource("revanced_debug_summary_off", "Debug logs are disabled")
                    ),
                    SwitchPreference(
                        "revanced_debug_protobuffer",
                        StringResource(
                            "revanced_debug_protobuffer_title",
                            "Log protocol buffer"
                        ),
                        StringResource("revanced_debug_protobuffer_summary_on", "Debug logs include proto buffer"),
                        StringResource("revanced_debug_protobuffer_summary_off", "Debug logs do not include proto buffer")
                    ),
                    SwitchPreference(
                        "revanced_debug_stacktrace",
                        StringResource(
                            "revanced_debug_stacktrace_title",
                            "Log stack traces"
                        ),
                        StringResource("revanced_debug_stacktrace_summary_on", "Debug logs include stack trace"),
                        StringResource("revanced_debug_stacktrace_summary_off", "Debug logs do not include stack trace")
                    ),
                    SwitchPreference(
                        "revanced_debug_toast_on_error",
                        StringResource(
                            "revanced_debug_toast_on_error_title",
                            "Show toast on ReVanced error"
                        ),
                        StringResource("revanced_debug_toast_on_error_summary_on", "Toast shown if error occurs"),
                        StringResource("revanced_debug_toast_on_error_summary_off", "Toast not shown if error occurs"),
                        StringResource("revanced_debug_toast_on_error_user_dialog_message",
                            "Turning off error toasts hides all ReVanced error notifications."
                                    + "\\n\\nYou will not be notified of any unexpected events."
                        )
                    ),
                ),
                StringResource("revanced_debug_preference_screen_summary", "Enable or disable debugging options")
            )
        )
    }
}
