package app.revanced.patches.youtube.misc.debugging.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.debugging.annotations.DebuggingCompatibility
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.w3c.dom.Element

@Patch
@Name("debugging")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Description("Adds debugging options.")
@DebuggingCompatibility
@Version("0.0.1")
class DebuggingPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            app.revanced.patches.shared.settings.preference.impl.PreferenceScreen(
                "revanced_debug",
                StringResource("revanced_debug_title", "Debugging"),
                listOf(
                    SwitchPreference(
                        "revanced_debug_enabled",
                        StringResource("revanced_debug_enabled_title", "Debug logging"),
                        false,
                        StringResource("revanced_debug_summary_on", "Debug logs are enabled"),
                        StringResource("revanced_debug_summary_off", "Debug logs are disabled")
                    ),
                    SwitchPreference(
                        "revanced_debug_stacktrace_enabled",
                        StringResource(
                            "revanced_debug_stacktrace_enabled_title",
                            "Log stack traces"
                        ),
                        false,
                        StringResource("revanced_debug_stacktrace_summary_on", "Debug logs include stack trace"),
                        StringResource("revanced_debug_stacktrace_summary_off", "Debug logs do not include stack trace")
                    ),
                    SwitchPreference(
                        "revanced_debug_toast_on_error_enabled",
                        StringResource(
                            "revanced_debug_toast_on_error_enabled_title",
                            "Show toast on ReVanced error"
                        ),
                        true,
                        StringResource("revanced_debug_toast_on_error_summary_on", "Toast shown if error occurs"),
                        StringResource("revanced_debug_toast_on_error_summary_off", "Toast not shown if error occurs"),
                        StringResource("revanced_debug_toast_on_error_user_dialog_message",
                            "Turning off error toasts hides all ReVanced error notifications." +
                                    " This includes hiding normal network connection timeouts, " +
                                    "but also hides notification of any unexpected and more serious errors."
                        )
                    ),
                ),
                StringResource("revanced_debug_summary", "Enable or disable debugging options")
            )
        )

        if (debuggable == true) {
            context.xmlEditor["AndroidManifest.xml"].use { dom ->
                val applicationNode = dom
                    .file
                    .getElementsByTagName("application")
                    .item(0) as Element

                // set application as debuggable
                applicationNode.setAttribute("android:debuggable", "true")
            }
        }

        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        var debuggable: Boolean? by option(
            PatchOption.BooleanOption(
                key = "debuggable",
                default = false,
                title = "App debugging",
                description = "Whether to make the app debuggable on Android.",
            )
        )
    }
}
