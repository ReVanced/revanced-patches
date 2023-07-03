package app.revanced.patches.music.misc.androidauto.patch

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
import app.revanced.patches.music.annotations.MusicCompatibility
import app.revanced.patches.music.misc.androidauto.fingerprints.CheckCertificateFingerprint

@Patch
@Name("bypass-certificate-checks")
@Description("Bypasses certificate checks which prevent YouTube Music from working on Android Auto.")
@MusicCompatibility
@Version("0.0.1")
class BypassCertificateChecksPatch : BytecodePatch(
    listOf(CheckCertificateFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        CheckCertificateFingerprint.result?.apply {
            mutableMethod.addInstructions(
                0, """
                const/4 v0, 0x1
                return v0
                """
            )
        } ?: return CheckCertificateFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
