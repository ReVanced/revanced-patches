package app.revanced.patches.youtube.layout.buttons.cast

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

val hideCastButtonPatch = bytecodePatch(
    name = "Hide cast button",
    description = "Adds an option to hide the cast button in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith("com.google.android.youtube")

    execute { context ->
        addResources("youtube", "layout.buttons.cast.hideCastButtonPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_cast_button"),
        )

        val buttonClass = context.classByType("MediaRouteButton")
            ?: throw PatchException("MediaRouteButton class not found.")

        buttonClass.mutableClass.methods.find { it.name == "setVisibility" }?.addInstructions(
            0,
            """
                invoke-static {p1}, Lapp/revanced/extension/youtube/patches/HideCastButtonPatch;->getCastButtonOverrideV2(I)I
                move-result p1
            """,
        ) ?: throw PatchException("setVisibility method not found.")
    }
}
