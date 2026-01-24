package app.revanced.patches.twitch.debug

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.misc.extension.sharedExtensionPatch
import app.revanced.patches.twitch.misc.settings.PreferenceScreen
import app.revanced.patches.twitch.misc.settings.Settings

@Suppress("ObjectPropertyName")
val `Debug mode` by creatingBytecodePatch(
    description = "Enables Twitch's internal debugging mode.",
    use = false,
) {
    dependsOn(
        sharedExtensionPatch,
        Settings,
        addResourcesPatch,
    )

    compatibleWith("tv.twitch.android.app"("16.9.1", "25.3.0"))

    apply {
        addResources("twitch", "debug.debugModePatch")

        PreferenceScreen.MISC.OTHER.addPreferences(
            SwitchPreference("revanced_twitch_debug_mode"),
        )

        listOf(
            isDebugConfigEnabledMethod,
            isOmVerificationEnabledMethod,
            shouldShowDebugOptionsMethod,
        ).forEach { method ->
            method.addInstructions(
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
