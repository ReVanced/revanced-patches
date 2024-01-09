package app.revanced.patches.cieid.restrictions.root

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.cieid.restrictions.root.fingerprints.CheckRootFingerprint

@Patch(
    name = "Bypass root checks",
    description = "Removes the restriction to use the app with root permissions or on a custom ROM.",
    compatiblePackages = [CompatiblePackage("it.ipzs.cieid")]
)
@Suppress("unused")
object BypassRootChecksPatch : BytecodePatch(
    setOf(CheckRootFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        CheckRootFingerprint.result?.mutableMethod?.addInstruction(1, "return-void")
            ?: throw CheckRootFingerprint.exception
    }
}