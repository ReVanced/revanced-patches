package app.revanced.patches.music.misc.androidauto

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.misc.androidauto.fingerprints.CheckCertificateFingerprint


@Patch(
    name = "Bypass certificate checks",
    description = "Bypasses certificate checks which prevent YouTube Music from working on Android Auto.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object BypassCertificateChecksPatch : BytecodePatch(setOf(CheckCertificateFingerprint)) {
    override fun execute(context: BytecodeContext) {
        CheckCertificateFingerprint.result?.apply {
            mutableMethod.addInstructions(
                0, """
                const/4 v0, 0x1
                return v0
                """
            )
        } ?: throw CheckCertificateFingerprint.exception
    }
}
