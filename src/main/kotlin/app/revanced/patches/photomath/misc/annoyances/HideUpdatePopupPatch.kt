package app.revanced.patches.photomath.misc.annoyances

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.photomath.detection.signature.SignatureDetectionPatch
import app.revanced.patches.photomath.misc.annoyances.fingerprints.HideUpdatePopupFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide update popup",
    description = "Prevents the update popup from showing up.",
    dependencies = [SignatureDetectionPatch::class],
    compatiblePackages = [CompatiblePackage("com.microblink.photomath", ["8.32.0"])]
)
@Suppress("unused")
object HideUpdatePopupPatch : BytecodePatch(
    setOf(HideUpdatePopupFingerprint)
) {
    override fun execute(context: BytecodeContext) = HideUpdatePopupFingerprint.result?.mutableMethod?.addInstructions(
        2, // Insert after the null check.
        "return-void"
    ) ?: throw HideUpdatePopupFingerprint.exception
}