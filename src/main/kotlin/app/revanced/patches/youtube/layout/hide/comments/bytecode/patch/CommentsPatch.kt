package app.revanced.patches.youtube.layout.hide.comments.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.comments.annotations.CommentsCompatibility
import app.revanced.patches.youtube.layout.hide.comments.bytecode.fingerprints.ShortsCommentsButtonFingerprint
import app.revanced.patches.youtube.layout.hide.comments.resource.patch.CommentsResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, CommentsResourcePatch::class])
@Name("comments")
@Description("Hides components related to comments.")
@CommentsCompatibility
@Version("0.0.1")
class CommentsPatch : BytecodePatch(
    listOf(
        ShortsCommentsButtonFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val checkCastAnchorFingerprint = object : MethodFingerprint(
            opcodes = listOf(
                Opcode.CONST,
                Opcode.CONST_HIGH16,
                Opcode.IF_EQZ,
                Opcode.CONST,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
            )
        ) {}

        ShortsCommentsButtonFingerprint.result?.let {
            it.mutableMethod.apply {
                val checkCastAnchorIndex = checkCastAnchorFingerprint.also { result ->
                    if (!result.resolve(context, this, it.classDef))
                        throw checkCastAnchorFingerprint.toErrorResult()
                }.result!!.scanResult.patternScanResult!!.endIndex

                val shortsCommentsButtonRegister = instruction<OneRegisterInstruction>(checkCastAnchorIndex).registerA
                val insertIndex = checkCastAnchorIndex + 1

                addInstructions(
                    insertIndex,
                    """
                    invoke-static {v$shortsCommentsButtonRegister, Lapp/revanced/integrations/patches/HideShortsCommentsButtonPatch;->hideShortsCommentsButton(Landroid/view/View;)V
                """
                )
            }
        } ?: return ShortsCommentsButtonFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
