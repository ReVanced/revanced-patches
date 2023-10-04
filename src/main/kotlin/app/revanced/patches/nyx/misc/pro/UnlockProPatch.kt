package app.revanced.patches.nyx.misc.pro

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.nyx.misc.pro.fingerprints.CheckProFingerprint

@Patch(
    name = "Unlock pro",
    compatiblePackages = [CompatiblePackage("com.awedea.nyx")]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(setOf(CheckProFingerprint)) {
    override fun execute(context: BytecodeContext) = CheckProFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
    ) ?: throw CheckProFingerprint.exception
}