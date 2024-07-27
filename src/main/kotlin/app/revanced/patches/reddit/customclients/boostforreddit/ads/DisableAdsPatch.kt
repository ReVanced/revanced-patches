package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.reddit.customclients.boostforreddit.ads.fingerprints.*
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions

@Suppress("unused")
object DisableAdsPatch : BytecodePatch(
    name = "Disable ads",
    compatiblePackages = setOf(CompatiblePackage("com.rubenmayayo.reddit")),
    fingerprints = setOf(MaxMediationFingerprint, AdmobMediationFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        MaxMediationFingerprint.result?.mutableMethod?.addInstructions(0, "return-void")
            ?: throw PatchException("MaxMediationFingerprint not found")
        AdmobMediationFingerprint.result?.mutableMethod?.addInstructions(0, "return-void")
            ?: throw PatchException("AdmobMediationFingerprint not found")
    }
}
