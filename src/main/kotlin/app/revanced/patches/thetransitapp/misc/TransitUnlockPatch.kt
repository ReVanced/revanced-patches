package app.revanced.patches.thetransitapp.misc
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.thetransitapp.misc.fingerprints.IsPremiumFingerprint

@Patch(
    name = "Pro Features Unlock",
    description = "Unlock all pro features in Transit",
    compatiblePackages = [
        CompatiblePackage("com.thetransitapp.droid", ["5.15.16"]),
    ],
)
@Suppress("unused")
object TransitUnlockPatch :  BytecodePatch(setOf(IsPremiumFingerprint)) {
    override fun execute(context: BytecodeContext) = IsPremiumFingerprint.result?.let { result ->
        // put v2 var with true
        result.mutableMethod.replaceInstruction(6, "const/4 v2, 0x1")
    } ?: throw IllegalStateException("Fingerprint not found")
}