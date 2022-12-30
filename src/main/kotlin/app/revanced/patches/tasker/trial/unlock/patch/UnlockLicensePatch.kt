package app.revanced.patches.tasker.trial.unlock.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tasker.trial.unlock.annotations.UnlockTrialCompatibility
import app.revanced.patches.tasker.trial.unlock.fingerprints.CheckLicenseFingerprint

@Patch
@Name("unlock-trial")
@Description("Unlocks the trial version.")
@UnlockTrialCompatibility
@Version("0.0.1")
class UnlockLicensePatch : BytecodePatch(
    listOf(
        CheckLicenseFingerprint
    )
) {
    override fun execute(context: BytecodeContext) = CheckLicenseFingerprint
        .result
        ?.mutableMethod
        // Return the method early, which prompts the user with a non dismissible dialog, when the trial period is over.
        ?.addInstruction(0, "return-void")
        ?.let { PatchResultSuccess() }
        ?: CheckLicenseFingerprint.toErrorResult()
}