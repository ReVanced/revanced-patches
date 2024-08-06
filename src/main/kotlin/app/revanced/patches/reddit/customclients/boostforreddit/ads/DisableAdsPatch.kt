package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.reddit.customclients.boostforreddit.ads.fingerprints.*
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable ads",
    compatiblePackages = [CompatiblePackage("com.rubenmayayo.reddit")],
)
@Suppress("unused")
object DisableAdsPatch : BytecodePatch(
    setOf(MaxMediationFingerprint, AdmobMediationFingerprint),
) {
    override fun execute(context: BytecodeContext) =
        arrayOf(MaxMediationFingerprint, AdmobMediationFingerprint).forEach {
            it.resultOrThrow().mutableMethod.addInstructions(0, "return-void")
        }
}
