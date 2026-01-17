package app.revanced.patches.reddit.customclients.sync.syncforreddit.annoyances.startup

import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Disable Sync for Lemmy bottom sheet` = creatingBytecodePatch(
    description = "Disables the bottom sheet at the startup that asks you to signup to \"Sync for Lemmy\".",
) {
    compatibleWith(
        "com.laurencedawson.reddit_sync"("v23.06.30-13:39"),
        "com.laurencedawson.reddit_sync.pro"(), // Version unknown.
        "com.laurencedawson.reddit_sync.dev"(), // Version unknown.
    )

    apply {
        val showBottomSheetIndex = mainActivityOnCreateMethod.instructions.lastIndex - 1

        mainActivityOnCreateMethod.removeInstruction(showBottomSheetIndex)
    }
}
