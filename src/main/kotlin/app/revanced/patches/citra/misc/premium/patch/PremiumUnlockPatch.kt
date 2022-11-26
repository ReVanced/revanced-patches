package app.revanced.patches.citra.misc.premium.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.citra.misc.premium.annotations.PremiumUnlockCompatbility
import app.revanced.patches.citra.misc.premium.fingerprints.PremiumUnlockFingerprint

@Patch
@Name("premium-unlock")
@Description("Unlocks premium functions.")
@PremiumUnlockCompatbility
@Version("0.0.1")
class PremiumUnlockPatch : BytecodePatch(
    listOf(PremiumUnlockFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = PremiumUnlockFingerprint.result ?: return PatchResultError("${PremiumUnlockFingerprint.name} not found")

        result.mutableMethod.addInstructions(
            0,
            """
                const v0, 0x1
                return v0
            """
        )
        return PatchResultSuccess()
    }
}