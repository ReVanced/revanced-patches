package app.revanced.patches.warnwetter.misc.firebasegetcert

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints.GetMessagingCertFingerprint
import app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints.GetReqistrationCertFingerprint

@Patch(
    description = "Spoofs the X-Android-Cert header.",
    compatiblePackages = [CompatiblePackage("de.dwd.warnapp")]
)
object FirebaseGetCertPatch : BytecodePatch(
    setOf(
        GetReqistrationCertFingerprint,
        GetMessagingCertFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        val spoofedInstruction =
            """
                const-string v0, "0799DDF0414D3B3475E88743C91C0676793ED450"
                return-object v0
            """

        val registrationCertMethod = GetReqistrationCertFingerprint.result!!.mutableMethod
        val messagingCertMethod = GetMessagingCertFingerprint.result!!.mutableMethod

        registrationCertMethod.addInstructions(
            0,
            spoofedInstruction
        )
        messagingCertMethod.addInstructions(
            0,
            spoofedInstruction
        )
    }
}