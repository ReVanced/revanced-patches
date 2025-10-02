package app.revanced.patches.youtube.layout.hide.signintotvpopup

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var mdx_seamless_tv_sign_in_drawer_fragment_title_id = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableSignInToTvPopupPatch;"

val disableSignInToTvPopupPatch = bytecodePatch(
    name = "Disable sign in to TV popup",
    description = "Adds an option to disable the popup asking to sign into a TV on the same local network.",
) {
    dependsOn(
        settingsPatch,
        sharedExtensionPatch,
        addResourcesPatch,
        resourceMappingPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.hide.signintotv.disableSignInToTvPopupPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_disable_signin_to_tv_popup"),
        )

        mdx_seamless_tv_sign_in_drawer_fragment_title_id = resourceMappings[
            "string",
            "mdx_seamless_tv_sign_in_drawer_fragment_title",
        ]

        signInToTvPopupFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->disableSignInToTvPopup()Z
                move-result v0
                if-eqz v0, :allow_sign_in_popup
                const/4 v0, 0x0
                return v0
                :allow_sign_in_popup
                nop
            """
        )
    }
}
