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
                listOf(
                    SwitchPreference("revanced_debug"),
                    SwitchPreference("revanced_debug_protobuffer"),
                    SwitchPreference("revanced_debug_stacktrace"),
                    SwitchPreference("revanced_debug_toast_on_error")
                )
            )
        )
    }
}
