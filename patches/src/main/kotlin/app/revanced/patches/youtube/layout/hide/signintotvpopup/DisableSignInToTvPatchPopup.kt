package app.revanced.patches.youtube.layout.hide.signintotvpopup

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch


val disableSignInToTvPopupPatch = bytecodePatch(
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
        addResources("youtube", "layout.hide.signintotv.disableSignInToTvPopupPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_disable_signin_to_tv_popup"),
        )

        signInToTvPopupFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/extension/youtube/patches/DisableSignInToTvPopupPatch;->disableSignInToTvPopup()Z
                move-result v0
                if-eqz v0, :disable_signintotvpopup
                const/4 v0, 0x0
                return v0
                :disable_signintotvpopup
                nop
            """,
        )
    }
}
