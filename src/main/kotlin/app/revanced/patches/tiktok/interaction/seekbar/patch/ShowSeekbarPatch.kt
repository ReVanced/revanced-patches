package app.revanced.patches.tiktok.interaction.seekbar.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.interaction.seekbar.annotations.ShowSeekbarCompatibility
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.AwemeGetVideoControlFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c

@Patch
@Name("show-seekbar")
@Description("Shows progress bar for all video.")
@ShowSeekbarCompatibility
@Version("0.0.1")
class ShowSeekbarPatch : BytecodePatch(
    listOf(
        AwemeGetVideoControlFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        //Get VideoControl FieldReference
        val videoControl = context.findClass { it.type.endsWith("/VideoControl;") }
            ?: return PatchResultError("Can not find target class")
        val fieldList = videoControl.immutableClass.fields.associateBy { field -> field.name }

        AwemeGetVideoControlFingerprint.result?.mutableMethod?.implementation?.apply {
            val ifNullLabel = newLabelForIndex(1)
            addInstructions(
                1,
                listOf(
                    BuilderInstruction11n(Opcode.CONST_4, 1, 1),
                    BuilderInstruction21t(Opcode.IF_EQZ, 0, ifNullLabel),
                    BuilderInstruction22c(Opcode.IPUT, 1, 0, fieldList["showProgressBar"]!!),
                    BuilderInstruction22c(Opcode.IPUT, 1, 0, fieldList["draftProgressBar"]!!)
                )
            )
        } ?: return AwemeGetVideoControlFingerprint.toErrorResult()
        return PatchResultSuccess()
    }

}