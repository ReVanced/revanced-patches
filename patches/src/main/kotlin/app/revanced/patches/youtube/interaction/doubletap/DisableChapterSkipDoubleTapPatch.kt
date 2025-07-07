package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val disableChapterSkipDoubleTapPatch = bytecodePatch(
    name = "Disable double tap chapter skip",
    description = "Prevents the double tap gesture from ever skipping to the next/previous chapter.",
    use = false,
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
        val classDef = chapterSeekResultToStringFingerprint.classDef

        chapterSeekResultCtorFingerprint.match(classDef).method.apply {
            // Resets the isSeekingToChapterStart flag to false
            addInstruction(0, "const/4 p1, 0x0")
        }
    }
}