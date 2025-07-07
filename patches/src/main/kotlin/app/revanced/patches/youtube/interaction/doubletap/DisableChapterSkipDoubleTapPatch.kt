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
    name = "Disable double tap chapter skip",
    description = "Prevents the double tap gesture from ever skipping to the next/previous chapter.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            // Possibly earlier too, but I don't care about that personally
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "interaction.doubletap.disableChapterSkipDoubleTapPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_chapter_skip_double_tap"),
        )

        val classDef = chapterSeekResultToStringFingerprint.classDef

        chapterSeekResultCtorFingerprint.match(classDef).method.apply {
            // Resets the isSeekingToChapterStart flag to false
            addInstructions(
                0,
                """
                    invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->disableDoubleTapChapters(Z)Z
                    move-result p1
                """,
            )
        }
    }
}