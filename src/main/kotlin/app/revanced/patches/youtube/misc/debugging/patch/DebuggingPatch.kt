package app.revanced.patches.youtube.misc.debugging.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.debugging.annotations.DebuggingCompatibility
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@Name("Enable debugging")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Description("Adds debugging options.")
@DebuggingCompatibility
class DebuggingPatch : ResourcePatch {
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
