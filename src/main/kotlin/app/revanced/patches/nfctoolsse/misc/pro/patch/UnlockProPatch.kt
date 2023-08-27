package app.revanced.patches.nfctoolsse.misc.pro.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.nfctoolsse.misc.pro.annotations.UnlockProCompatibility
import app.revanced.patches.nfctoolsse.misc.pro.fingerprints.IsLicenseRegisteredFingerprint


@Patch
@Name("Unlock pro")
@Description("Unlocks all pro features.")
@UnlockProCompatibility
class UnlockProPatch : BytecodePatch(
    listOf(
        IsLicenseRegisteredFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        IsLicenseRegisteredFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        } ?: throw IsLicenseRegisteredFingerprint.exception
    }

}