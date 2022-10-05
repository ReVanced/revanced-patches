package app.revanced.patches.youtube.misc.enabledebugging.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.enabledebugging.annotations.EnableDebuggingCompatibility
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.w3c.dom.Element

@Patch(false)
@Name("enable-debugging")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Description("Enables app debugging by patching the manifest file.")
@EnableDebuggingCompatibility
@Version("0.0.1")
class EnableDebuggingPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_debug_enabled",
                StringResource("revanced_debug_title", "Debug"),
                false,
                StringResource("revanced_debug_on", "Debug logs are enabled"),
                StringResource("revanced_debug_off", "Debug logs are disabled")
            )
        )

        // create an xml editor instance
        context.xmlEditor["AndroidManifest.xml"].use { dom ->
            // get the application node
            val applicationNode = dom
                .file
                .getElementsByTagName("application")
                .item(0) as Element

            // set application as debuggable
            applicationNode.setAttribute("android:debuggable", "true")
        }

        return PatchResultSuccess()
    }
}
