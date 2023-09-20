package app.revanced.patches.myexpenses.misc.pro

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.myexpenses.misc.pro.fingerprints.IsEnabledFingerprint

@Patch(
    name = "Unlock pro",
    compatiblePackages = [CompatiblePackage("org.totschnig.myexpenses", ["3.4.9"])]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(setOf(IsEnabledFingerprint)) {
    override fun execute(context: BytecodeContext) = IsEnabledFingerprint.result?.mutableMethod?.addInstructions(
        0,
        """
            const/4 v0, 0x1
            return v0
        """
    ) ?: throw IsEnabledFingerprint.exception

}
