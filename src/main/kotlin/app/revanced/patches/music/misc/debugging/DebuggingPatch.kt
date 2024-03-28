package app.revanced.patches.music.misc.debugging

import app.revanced.patches.music.misc.integrations.IntegrationsPatch
import app.revanced.patches.music.misc.settings.SettingsPatch
import app.revanced.patches.shared.misc.debugging.BaseDebuggingPatch

@Suppress("unused")
object DebuggingPatch : BaseDebuggingPatch(
    integrationsPatch = IntegrationsPatch::class,
    settingsPatch = SettingsPatch::class,
    compatiblePackages = setOf(CompatiblePackage("com.google.android.apps.youtube.music")),
    miscPreferenceScreen = SettingsPatch.PreferenceScreen.MISC,
)
