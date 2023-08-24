package app.revanced.patches.reddit.customclients.joeyforreddit.ads.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.customclients.joeyforreddit.ads.fingerprints.IsAdFreeUserFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.patch.DisablePiracyDetectionPatch

@Patch
@Name("Disable ads")
@DependsOn([DisablePiracyDetectionPatch::class])
@Compatibility([Package("o.o.joey")])
class DisableAdsPatch : BytecodePatch(listOf(IsAdFreeUserFingerprint)) {
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