package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.MandatoryLoginServiceFingerprint
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.MandatoryLoginServiceFingerprint2

@Patch(
    name = "Disable login requirement",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill"),
        CompatiblePackage("com.zhiliaoapp.musically")
    ]
)
@Suppress("unused")
object DisableLoginRequirementPatch : BytecodePatch(
    setOf(MandatoryLoginServiceFingerprint, MandatoryLoginServiceFingerprint2)
) {
    override fun execute(context: BytecodeContext) {
        listOf(
            MandatoryLoginServiceFingerprint,
            MandatoryLoginServiceFingerprint2
        ).forEach { fingerprint ->
            val method = fingerprint.result!!.mutableMethod
            method.addInstructions(
                0,
                """
                const/4 v0, 0x0
                return v0
            """
            )
        }
    }
}