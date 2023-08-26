package app.revanced.patches.tiktok.misc.login.disablerequirement.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.misc.login.disablerequirement.annotations.DisableLoginRequirementCompatibility
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.MandatoryLoginServiceFingerprint
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.MandatoryLoginServiceFingerprint2

@Patch
@Name("Disable login requirement")
@Description("Do not force login.")
@DisableLoginRequirementCompatibility
class DisableLoginRequirementPatch : BytecodePatch(
    listOf(
        MandatoryLoginServiceFingerprint,
        MandatoryLoginServiceFingerprint2
    )
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