package app.revanced.patches.tiktok.misc.forcelogin.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.misc.forcelogin.annotations.DisableForceLoginCompatibility
import app.revanced.patches.tiktok.misc.forcelogin.fingerprints.MandatoryLoginServiceFingerprint
import app.revanced.patches.tiktok.misc.forcelogin.fingerprints.MandatoryLoginServiceFingerprint2

@Patch
@Name("tiktok-force-login")
@Description("Do not force login.")
@DisableForceLoginCompatibility
@Version("0.0.1")
class DisableForceLoginPatch : BytecodePatch(
    listOf(
        MandatoryLoginServiceFingerprint,
        MandatoryLoginServiceFingerprint2
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
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
        return PatchResultSuccess()
    }
}