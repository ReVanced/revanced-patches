package app.revanced.patches.youtube.layout.fullscreenpanels.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.fullscreenpanels.annotations.FullscreenPanelsCompatibility
import app.revanced.patches.youtube.layout.fullscreenpanels.fingerprints.FullscreenViewAdderFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@Name("disable-fullscreen-panels")
@Description("Disables comments panel in fullscreen view.")
@FullscreenPanelsCompatibility
@Version("0.0.1")
class FullscreenPanelsRemovalPatch : BytecodePatch(
    listOf(
        FullscreenViewAdderFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val method = FullscreenViewAdderFingerprint.result?.mutableMethod!!
        val implementation = method.implementation!!

        val (visibilityCallIndex, visibilityCall) =
            implementation.instructions.withIndex()
                .first { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.name == ("setVisibility") }

        val gotoIndex =
            implementation.instructions.subList(0, visibilityCallIndex).indexOfLast { it.opcode == Opcode.GOTO }

        //force the if
        method.removeInstruction(gotoIndex)

        val visibilityIntRegister = (visibilityCall as FiveRegisterInstruction).registerD

        //set the visibility to GONE
        method.addInstruction(visibilityCallIndex - 1, "const/16 v$visibilityIntRegister, 0x8")

        return PatchResultSuccess()
    }
}
