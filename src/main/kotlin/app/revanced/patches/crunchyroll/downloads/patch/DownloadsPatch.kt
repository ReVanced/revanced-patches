package app.revanced.patches.crunchyroll.downloads.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.crunchyroll.downloads.annotations.DownloadsCompatibility
import app.revanced.patches.crunchyroll.downloads.fingerprints.DownloadsFingerprint

@Patch
@Name("enable-downloads")
@Description("Enables downloads for Crunchyroll.")
@DownloadsCompatibility
@Version("0.0.1")
class DownloadsPatch : BytecodePatch(
    listOf(
        DownloadsFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        with(DownloadsFingerprint.result!!.mutableMethod) {
            val index = implementation!!.instructions.lastIndex
            replaceInstruction(
                index - 1,
                """
                   const/4 v0, 0x1 
                """
            )
        }
        return PatchResultSuccess()
    }
}