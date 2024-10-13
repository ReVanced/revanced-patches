package app.revanced.patches.youtube.layout.hide.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.seekbarColorPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.seekbarFingerprint
import app.revanced.patches.youtube.shared.seekbarOnDrawFingerprint
import app.revanced.util.matchOrThrow

@Suppress("unused")
val hideSeekbarPatch = bytecodePatch(
    name = "Hide seekbar",
    description = "Adds an option to hide the seekbar.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        seekbarColorPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val seekbarMatch by seekbarFingerprint()

    execute { context ->
        addResources("youtube", "layout.hide.seekbar.hideSeekbarPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_hide_seekbar"),
            SwitchPreference("revanced_hide_seekbar_thumbnail"),
        )

        seekbarOnDrawFingerprint.apply {
            match(context, seekbarMatch.mutableClass)
        }.matchOrThrow().mutableMethod.addInstructionsWithLabels(
            0,
            """
                const/4 v0, 0x0
                invoke-static { }, Lapp/revanced/extension/youtube/patches/HideSeekbarPatch;->hideSeekbar()Z
                move-result v0
                if-eqz v0, :hide_seekbar
                return-void
                :hide_seekbar
                nop
            """,
        )
    }
}
