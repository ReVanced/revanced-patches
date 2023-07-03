package app.revanced.patches.reddit.misc.tracking.url.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.misc.tracking.url.annotations.SanitizeUrlQueryCompatibility
import app.revanced.patches.reddit.misc.tracking.url.fingerprints.ShareLinkFormatterFingerprint

@Patch
@Name("sanitize-sharing-links")
@Description("Removes (tracking) query parameters from the URLs when sharing links.")
@SanitizeUrlQueryCompatibility
@Version("0.0.1")
class SanitizeUrlQueryPatch : BytecodePatch(
    listOf(ShareLinkFormatterFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {

        ShareLinkFormatterFingerprint.result?.mutableMethod?.addInstructions(
            0,
            "return-object p0"
        ) ?: return ShareLinkFormatterFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

}
