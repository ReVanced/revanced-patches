package app.revanced.patches.tiktok.misc.login.disablerequirement.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.misc.login.disablerequirement.annotations.DisableLoginRequirementCompatibility
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.MandatoryLoginServiceFingerprint
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.MandatoryLoginServiceFingerprint2

@Patch
@Name("disable-login-requirement")
@Description("Do not force login.")
@DisableLoginRequirementCompatibility
@Version("0.0.1")
class DisableLoginRequirementPatch : BytecodePatch(
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