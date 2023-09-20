package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.misc.tracking.url.fingerprints.ShareLinkFormatterFingerprint


@Patch(
    name = "Sanitize sharing links",
    description = "Removes (tracking) query parameters from the URLs when sharing links.",
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object SanitizeUrlQueryPatch : BytecodePatch(setOf(ShareLinkFormatterFingerprint)) {
    override fun execute(context: BytecodeContext) {

        ShareLinkFormatterFingerprint.result?.mutableMethod?.addInstructions(
            0,
            "return-object p0"
        ) ?: throw ShareLinkFormatterFingerprint.exception
    }
}