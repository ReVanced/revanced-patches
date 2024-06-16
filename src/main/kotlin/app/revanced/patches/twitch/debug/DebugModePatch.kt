package app.revanced.patches.twitch.debug

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.misc.integrations.integrationsPatch
import app.revanced.patches.twitch.misc.settings.PreferenceScreen
import app.revanced.patches.twitch.misc.settings.settingsPatch

@Suppress("unused")
val debugModePatch = bytecodePatch(
    name = "Debug mode",
    description = "Enables Twitch's internal debugging mode.",
    use = false,
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith("tv.twitch.android.app")

    val isDebugConfigEnabledFingerprintResult by isDebugConfigEnabledFingerprint()
    val isOmVerificationEnabledFingerprintResult by isOmVerificationEnabledFingerprint()
    val shouldShowDebugOptionsFingerprintResult by shouldShowDebugOptionsFingerprint()

    execute {
        addResources("twitch", "debug.debugModePatch")

        PreferenceScreen.MISC.OTHER.addPreferences(
            SwitchPreference("revanced_twitch_debug_mode"),
        )

        listOf(
            isDebugConfigEnabledFingerprintResult,
            isOmVerificationEnabledFingerprintResult,
            shouldShowDebugOptionsFingerprintResult,
        ).forEach {
            it.mutableMethod.addInstructions(
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
