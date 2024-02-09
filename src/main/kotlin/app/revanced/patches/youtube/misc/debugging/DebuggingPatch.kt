package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.debugging.BaseDebuggingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Suppress("unused")
object DebuggingPatch : BaseDebuggingPatch(
    integrationsPatch = IntegrationsPatch::class,
    settingsPatch = SettingsPatch::class,
    compatiblePackages = setOf(CompatiblePackage("com.google.android.youtube")),
    miscPreferenceScreen = SettingsPatch.PreferenceScreen.MISC,
    additionalDebugPreferences = setOf(
        SwitchPreference("revanced_debug_protobuffer")
    ),
    additionalDependencies = setOf(AddResourcesPatch::class)
) {
    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        super.execute(context)
    }
}
