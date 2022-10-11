package app.revanced.patches.youtube.layout.theme.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.layout.theme.annotations.ThemeCompatibility
import app.revanced.patches.youtube.layout.theme.fingerprints.CommentsFilterBarFingerprint

@Name("comment-filter-bar-theme")
@Description("Applies custom theming to comments filter action bar.")
@ThemeCompatibility
@Version("0.0.1")
class CommentsFilterBarPatch : BytecodePatch(
    listOf(
        CommentsFilterBarFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = CommentsFilterBarFingerprint.result!!
        val method = result.mutableMethod
        val patchIndex = result.scanResult.patternScanResult!!.endIndex - 1

        method.addInstructions(
            patchIndex, """
                const v1, -0x1
                if-ne v1, p1, :comments_filter_white
                const v1, -0xdededf
                if-ne v1, p1, :comments_filter_dark
                const/4 p1, 0x0
                :comments_filter_white
            """, listOf(ExternalLabel("comments_filter_dark", method.instruction(patchIndex)))
        )
        return PatchResultSuccess()
    }
}
