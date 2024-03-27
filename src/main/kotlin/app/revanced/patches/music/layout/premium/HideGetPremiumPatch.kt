package app.revanced.patches.music.layout.premium

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumParentFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide get premium",
    description = "Removes all \"Get Premium\" evidences from the avatar menu.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object HideGetPremiumPatch : BytecodePatch(
    setOf(HideGetPremiumParentFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        HideGetPremiumParentFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex + 1
                val register = getInstruction<TwoRegisterInstruction>(insertIndex - 1).registerA

                addInstruction(insertIndex, "const/4 v$register, 0x0")
            }
        } ?: throw HideGetPremiumParentFingerprint.exception

        val parentResult = HideGetPremiumParentFingerprint.result!!
        HideGetPremiumFingerprint.resolve(context, parentResult.classDef)

        val startIndex = parentResult.scanResult.patternScanResult!!.startIndex
        HideGetPremiumFingerprint.result!!.mutableMethod.addInstruction(
            startIndex,
            """
                const/16 v0, 0x8
            """,
        )
    }
}
