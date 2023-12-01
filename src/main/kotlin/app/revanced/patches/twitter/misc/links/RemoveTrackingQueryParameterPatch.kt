package app.revanced.patches.twitter.misc.links

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.links.fingerprints.AddTrackingQueryToLinkFingerprint

@Patch(
    name = "Remove tracking query parameter",
    description = "Remove the tracking query parameter from links.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object RemoveTrackingQueryParameterPatch : BytecodePatch(
    setOf(AddTrackingQueryToLinkFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AddTrackingQueryToLinkFingerprint.result?.mutableMethod?.addInstruction(0, "return-object p0")
            ?: throw AddTrackingQueryToLinkFingerprint.exception
    }
}
