package app.revanced.patches.music.utils.fix.androidauto

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.fix.androidauto.fingerprints.CertificateCheckFingerprint
import app.revanced.util.exception

@Patch(
    name = "Certificate spoof",
    description = "Enables YouTube Music to work with Android Auto by spoofing the YouTube Music certificate.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object AndroidAutoCertificatePatch : BytecodePatch(
    setOf(CertificateCheckFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        CertificateCheckFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        const/4 v0, 0x1
                        return v0
                        """
                )
            }
        } ?: throw CertificateCheckFingerprint.exception

    }
}
