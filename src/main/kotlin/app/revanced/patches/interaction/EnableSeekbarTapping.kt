package app.revanced.patches.interaction

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.writer.ASMWriter.insertAt
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class EnableSeekbarTapping : Patch("enable-seekbar-tapping") {
    override fun execute(cache: Cache): PatchResult {
        val patchData = cache.methods["enable-seekbar-tapping"]
        val methodOPatchData = cache.methods["enable-seekbar-tapping-method-o"]
        val methodPPatchData = cache.methods["enable-seekbar-tapping-method-p"]

        val elseLabel = LabelNode()
        patchData.method.instructions.insertAt(
            patchData.scanData.endIndex,
            InsnNode(Opcodes.ACONST_NULL),
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "fi/razerman/youtube/preferences/BooleanPreferences",
                "isTapSeekingEnabled",
                "()Z"
            ),
            JumpInsnNode(Opcodes.IFEQ, elseLabel),
            VarInsnNode(Opcodes.ALOAD, 0),
            VarInsnNode(Opcodes.ILOAD, 6),
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                methodOPatchData.declaringClass.name,
                methodOPatchData.method.name,
                "(I)V"
            ),
            VarInsnNode(Opcodes.ALOAD, 0),
            VarInsnNode(Opcodes.ILOAD, 6),
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                methodPPatchData.declaringClass.name,
                methodPPatchData.method.name,
                "(I)V"
            ),
            elseLabel
        )

        return PatchResultSuccess()
    }
}