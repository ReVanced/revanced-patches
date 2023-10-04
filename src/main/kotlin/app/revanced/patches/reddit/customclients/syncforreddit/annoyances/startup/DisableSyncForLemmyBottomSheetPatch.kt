package app.revanced.patches.reddit.customclients.syncforreddit.annoyances.startup

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.annoyances.startup.fingerprints.MainActivityOnCreate


@Patch(
    name = "Disable Sync for Lemmy bottom sheet",
    description = "Disables the bottom sheet at the startup that asks you to signup to \"Sync for Lemmy\".",
    compatiblePackages = [
        CompatiblePackage("com.laurencedawson.reddit_sync", ["v23.06.30-13:39"]),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"), // Version unknown.
        CompatiblePackage("com.laurencedawson.reddit_sync.dev") // Version unknown.
    ]
)
@Suppress("unused")
object DisableSyncForLemmyBottomSheetPatch : BytecodePatch(setOf(MainActivityOnCreate)) {
    override fun execute(context: BytecodeContext) {
        MainActivityOnCreate.result?.mutableMethod?.apply {
            val showBottomSheetIndex = implementation!!.instructions.lastIndex - 1

            removeInstruction(showBottomSheetIndex)
        }  ?: throw MainActivityOnCreate.exception
    }
}