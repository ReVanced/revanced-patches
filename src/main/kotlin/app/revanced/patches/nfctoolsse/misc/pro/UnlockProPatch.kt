package app.revanced.patches.nfctoolsse.misc.pro

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.nfctoolsse.misc.pro.fingerprints.IsLicenseRegisteredFingerprint


@Patch(
    name = "Unlock pro",
    compatiblePackages = [CompatiblePackage("com.wakdev.apps.nfctools.se")]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(setOf(IsLicenseRegisteredFingerprint)) {
    override fun execute(context: BytecodeContext) = IsLicenseRegisteredFingerprint.result?.mutableMethod
        ?.addInstructions(
            0, """
                    const/4 v0, 0x1
                    return v0
                """
        ) ?: throw IsLicenseRegisteredFingerprint.exception
}
