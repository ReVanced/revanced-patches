package app.revanced.patches.twitch.debug

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.misc.extension.sharedExtensionPatch
import app.revanced.patches.twitch.misc.settings.PreferenceScreen
import app.revanced.patches.twitch.misc.settings.settingsPatch

@Suppress("unused")
val debugModePatch = bytecodePatch(
    name = "Debug mode",
    description = "Enables Twitch's internal debugging mode.",
    use = false,
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith("tv.twitch.android.app")

    val isDebugConfigEnabledMatch by isDebugConfigEnabledFingerprint()
    val isOmVerificationEnabledMatch by isOmVerificationEnabledFingerprint()
    val shouldShowDebugOptionsMatch by shouldShowDebugOptionsFingerprint()

    execute {
        addResources("twitch", "debug.debugModePatch")

        PreferenceScreen.MISC.OTHER.addPreferences(
            SwitchPreference("revanced_twitch_debug_mode"),
        )

        listOf(
            isDebugConfigEnabledMatch,
            isOmVerificationEnabledMatch,
            shouldShowDebugOptionsMatch,
        ).forEach {
            it.mutableMethod.addInstructions(
                0,
                """
                    invoke-static {}, Lapp/revanced/extension/twitch/patches/DebugModePatch;->isDebugModeEnabled()Z
                    move-result v0
                    return v0
                """,
            )
        }
    }
}
