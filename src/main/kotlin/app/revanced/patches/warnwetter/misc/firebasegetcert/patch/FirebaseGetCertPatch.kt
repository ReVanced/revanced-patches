package app.revanced.patches.warnwetter.misc.firebasegetcert.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints.*
import app.revanced.patches.warnwetter.misc.firebasegetcert.annotations.FirebaseGetCertPatchCompatibility

@Name("spoof-cert-patch")
@Description("Spoofs the X-Android-Cert header.")
@FirebaseGetCertPatchCompatibility
@Version("0.0.1")
class FirebaseGetCertPatch : BytecodePatch(
    listOf(
        GetReqistrationCertFingerprint,
        GetMessagingCertFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
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

        return PatchResultSuccess()
    }
}