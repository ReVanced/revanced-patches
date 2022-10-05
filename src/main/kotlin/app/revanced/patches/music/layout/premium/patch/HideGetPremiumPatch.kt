package app.revanced.patches.music.layout.premium.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.layout.premium.annotations.HideGetPremiumCompatibility
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumParentFingerprint

@Patch
@Name("hide-get-premium")
@Description("Removes all \"Get Premium\" evidences from the avatar menu.")
@HideGetPremiumCompatibility
@Version("0.0.1")
class HideGetPremiumPatch : BytecodePatch(
    listOf(
        HideGetPremiumParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val parentResult = HideGetPremiumParentFingerprint.result!!
        HideGetPremiumFingerprint.resolve(context, parentResult.classDef)

        val startIndex = parentResult.scanResult.patternScanResult!!.startIndex

        val parentMethod = parentResult.mutableMethod
        parentMethod.replaceInstruction(
            startIndex, """
            const/4 v1, 0x0
        """
        )

        val result = HideGetPremiumFingerprint.result!!
        val method = result.mutableMethod
        method.addInstructions(
            startIndex, """
            const/16 v0, 0x8
        """
        )

        return PatchResultSuccess()
    }
}
