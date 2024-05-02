package app.revanced.patches.twitch.debug

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.debug.fingerprints.isDebugConfigEnabledFingerprint
import app.revanced.patches.twitch.debug.fingerprints.isOmVerificationEnabledFingerprint
import app.revanced.patches.twitch.debug.fingerprints.shouldShowDebugOptionsFingerprint
import app.revanced.patches.twitch.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch

@Suppress("unused")
val debugModePatch = bytecodePatch(
    name = "Debug mode",
    description = "Enables Twitch's internal debugging mode.",
    use = false,
) {
    compatibleWith("tv.twitch.android.app"())

    dependsOn(integrationsPatch, SettingsPatch, addResourcesPatch)

    val isDebugConfigEnabledResult by isDebugConfigEnabledFingerprint
    val isOmVerificationEnabledResult by isOmVerificationEnabledFingerprint
    val shouldShowDebugOptionsResult by shouldShowDebugOptionsFingerprint

    execute {
        addResources(this)

        SettingsPatch.PreferenceScreen.MISC.OTHER.addPreferences(
            SwitchPreference("revanced_twitch_debug_mode"),
        )

        listOf(
            isDebugConfigEnabledResult,
            isOmVerificationEnabledResult,
            shouldShowDebugOptionsResult,
        ).forEach {
            it.mutableMethod.apply {
                addInstructions(
                    0,
                    """
                         invoke-static {}, Lapp/revanced/integrations/twitch/patches/DebugModePatch;->isDebugModeEnabled()Z
                         move-result v0
                         return v0
                      """,
                )
            }
        }
    }
}
