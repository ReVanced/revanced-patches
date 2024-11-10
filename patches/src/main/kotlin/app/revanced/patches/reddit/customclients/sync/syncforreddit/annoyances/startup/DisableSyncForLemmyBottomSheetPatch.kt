package app.revanced.patches.reddit.customclients.sync.syncforreddit.annoyances.startup

import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableSyncForLemmyBottomSheetPatch = bytecodePatch(
    name = "Disable Sync for Lemmy bottom sheet",
    description = "Disables the bottom sheet at the startup that asks you to signup to \"Sync for Lemmy\".",
) {
    compatibleWith(
        "com.laurencedawson.reddit_sync"("v23.06.30-13:39"),
        "com.laurencedawson.reddit_sync.pro"(), // Version unknown.
        "com.laurencedawson.reddit_sync.dev"(), // Version unknown.
    )

    execute {
        mainActivityOnCreateFingerprint.method.apply {
            val showBottomSheetIndex = implementation!!.instructions.lastIndex - 1

            removeInstruction(showBottomSheetIndex)
        }
    }
}
