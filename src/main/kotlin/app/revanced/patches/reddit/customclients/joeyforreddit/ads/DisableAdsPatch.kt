package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.joeyforreddit.ads.fingerprints.IsAdFreeUserFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.DisablePiracyDetectionPatch

@Patch(
    name = "Disable ads",
    dependencies = [DisablePiracyDetectionPatch::class],
    compatiblePackages = [CompatiblePackage("o.o.joey")]
)
@Suppress("unused")
object DisableAdsPatch : BytecodePatch(setOf(IsAdFreeUserFingerprint)) {
    override fun execute(context: BytecodeContext) {
        IsAdFreeUserFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        ) ?: throw IsAdFreeUserFingerprint.exception
    }
}