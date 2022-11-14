package app.revanced.patches.youtube.misc.debugging.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.debugging.annotations.DebuggingCompatibility
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
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
            SwitchPreference(
                "revanced_debug_enabled",
                StringResource("revanced_debug_title", "Debugging"),
                false,
                StringResource("revanced_debug_on", "Debug logs are enabled"),
                StringResource("revanced_debug_off", "Debug logs are disabled")
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
