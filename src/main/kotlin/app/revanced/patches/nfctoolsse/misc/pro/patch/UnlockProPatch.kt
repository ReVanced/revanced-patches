package app.revanced.patches.nfctoolsse.misc.pro.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.nfctoolsse.misc.pro.annotations.UnlockProCompatibility
import app.revanced.patches.nfctoolsse.misc.pro.fingerprints.IsLicenseRegisteredFingerprint


@Patch
@Name("unlock-pro")
@Description("Unlocks all pro features.")
@Version("0.0.1")
@UnlockProCompatibility
class UnlockProPatch : BytecodePatch(
    listOf(
        IsLicenseRegisteredFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        IsLicenseRegisteredFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        } ?: return IsLicenseRegisteredFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

}