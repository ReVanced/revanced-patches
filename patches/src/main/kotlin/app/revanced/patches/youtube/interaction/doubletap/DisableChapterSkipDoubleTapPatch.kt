package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableChapterSkipDoubleTapPatch;"

@Suppress("unused")
val disableChapterSkipDoubleTapPatch = bytecodePatch(
    name = "Disable double tap actions",
    description = "Adds an option to disable player double tap gestures.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "interaction.doubletap.disableChapterSkipDoubleTapPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_chapter_skip_double_tap"),
        )

        // Force isChapterSeek flag to false.
        doubleTapInfoGetSeekSourceFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->disableDoubleTapChapters(Z)Z
                move-result p1
            """
        )

        doubleTapInfoCtorFingerprint.match(
            doubleTapInfoGetSeekSourceFingerprint.classDef
        ).method.addInstructions(
            0,
            """
                invoke-static { p3 }, $EXTENSION_CLASS_DESCRIPTOR->disableDoubleTapChapters(Z)Z
                move-result p3
            """
        )
    }
}