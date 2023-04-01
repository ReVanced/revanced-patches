package app.revanced.patches.music.misc.androidauto.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.misc.androidauto.annotations.BypassCertificateChecksCompatibility
import app.revanced.patches.music.misc.androidauto.fingerprints.CheckCertificateFingerprint

@Patch
@Name("bypass-certificate-checks")
@Description("Bypasses certificate checks which prevent YouTube Music from working on Android Auto.")
@BypassCertificateChecksCompatibility
@Version("0.0.1")
class BypassCertificateChecksPatch : BytecodePatch(
    listOf(
        CheckCertificateFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        CheckCertificateFingerprint.result?.let { result ->
            val noMatchIndex = result.scanResult.stringsScanResult!!.matches.first().index

            result.mutableMethod.apply {
                val isPartnerIndex = noMatchIndex + 2

                replaceInstruction(isPartnerIndex, "const/4 p1, 0x1")
                addInstruction(isPartnerIndex + 1, "return p1")
            }
        } ?: return CheckCertificateFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
