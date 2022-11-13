package app.revanced.patches.twitch.chat.antidelete.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.twitch.chat.antidelete.annotations.ShowDeletedMessagesCompatibility
import app.revanced.patches.twitch.chat.antidelete.fingerprints.*
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x

@Patch
@Name("show-deleted-messages")
@Description("Shows deleted chat messages behind a clickable spoiler.")
@ShowDeletedMessagesCompatibility
@Version("0.0.1")
class ShowDeletedMessagesPatch : BytecodePatch(
    listOf(
        SetHasModAccessFingerprint,
        DeletedMessageClickableSpanCtorFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // Force set hasModAccess member to true in constructor
        with(DeletedMessageClickableSpanCtorFingerprint.result!!.mutableMethod) {
            addInstructions(
                implementation!!.instructions.lastIndex, /* place in front of return-void */
                """
                    const/4 v0, 1
                    iput-boolean v0, p0, $definingClass->hasModAccess:Z
                """
            )
        }

        // Disable setHasModAccess setter
        with(SetHasModAccessFingerprint.result!!.mutableMethod.implementation!!) {
            addInstruction(0, BuilderInstruction10x(Opcode.RETURN_VOID))
        }

        return PatchResultSuccess()
    }
}
