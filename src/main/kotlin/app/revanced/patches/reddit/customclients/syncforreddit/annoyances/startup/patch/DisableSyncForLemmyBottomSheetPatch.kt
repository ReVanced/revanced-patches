package app.revanced.patches.reddit.customclients.syncforreddit.annoyances.startup.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.annoyances.startup.fingerprints.MainActivityOnCreate

@Patch
@Name("Disable Sync for Lemmy bottom sheet")
@Description("Disables the bottom sheet at the startup that asks you to signup to \"Sync for Lemmy\".")
@Compatibility(
    [
        Package("com.laurencedawson.reddit_sync", ["v23.06.30-13:39"]),
        Package("com.laurencedawson.reddit_sync.pro"), // Version unknown.
        Package("com.laurencedawson.reddit_sync.dev") // Version unknown.
    ]
)
class DisableSyncForLemmyBottomSheetPatch : BytecodePatch(listOf(MainActivityOnCreate)) {
    override fun execute(context: BytecodeContext) {
        MainActivityOnCreate.result?.mutableMethod?.apply {
            val showBottomSheetIndex = implementation!!.instructions.lastIndex - 1

            removeInstruction(showBottomSheetIndex)
        }
    }
}