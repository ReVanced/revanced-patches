package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.links.fingerprints.SanitizeSharingLinksFingerprint
import app.revanced.util.exception

@Patch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from links before they are shared.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")],
)
object SanitizeSharingLinksPatch : BytecodePatch(
    setOf(SanitizeSharingLinksFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        SanitizeSharingLinksFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                # Method takes in a link (string, param 0) and then appends the tracking query params,
                # so all we need to do is return back the passed-in string
                return-object p0
            """,
        ) ?: throw SanitizeSharingLinksFingerprint.exception
    }
}