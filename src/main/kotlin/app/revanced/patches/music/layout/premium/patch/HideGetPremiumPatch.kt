package app.revanced.patches.music.layout.premium.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.annotations.MusicCompatibility
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumParentFingerprint

@Patch
@Name("Hide get premium")
@Description("Removes all \"Get Premium\" evidences from the avatar menu.")
@MusicCompatibility
class HideGetPremiumPatch : BytecodePatch(
    listOf(HideGetPremiumParentFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val parentResult = HideGetPremiumParentFingerprint.result!!
        HideGetPremiumFingerprint.resolve(context, parentResult.classDef)

        val startIndex = parentResult.scanResult.patternScanResult!!.startIndex

        val parentMethod = parentResult.mutableMethod
        parentMethod.replaceInstruction(
            startIndex,
            """
                const/4 v1, 0x0
            """
        )

        val result = HideGetPremiumFingerprint.result!!
        val method = result.mutableMethod
        method.addInstruction(
            startIndex,
            """
                const/16 v0, 0x8
            """
        )
    }
}
