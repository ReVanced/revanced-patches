package app.revanced.patches.youtube.layout.hide.signintotv

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch


val disableSignInToTvPatch = bytecodePatch(
    name = "Disable sign in to TV popup",
    description = "Adds an option to disable the popup asking to sign into a TV on the same local network.",
) {
    dependsOn(
        settingsPatch,
        sharedExtensionPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "layout.hide.signintotv.disableSignInToTvPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_disable_signin_to_tv"),
        )

        signInToTvFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/extension/youtube/patches/DisableSignInToTvPatch;->disableSignInToTv()Z
                move-result v0
                if-eqz v0, :disable_signintotv
                const/4 v0, 0x0
                return v0
                :disable_signintotv
                nop
            """,
        )
    }
}
