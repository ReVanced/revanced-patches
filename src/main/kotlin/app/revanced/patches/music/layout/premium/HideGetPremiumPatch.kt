package app.revanced.patches.music.layout.premium

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumParentFingerprint

@Patch(
    name = "Hide get premium",
    description = "Removes all \"Get Premium\" evidences from the avatar menu.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object HideGetPremiumPatch : BytecodePatch(setOf(HideGetPremiumParentFingerprint)) {
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
