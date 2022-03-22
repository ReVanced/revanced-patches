package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.signature.Signature
import app.revanced.patcher.util.ExtraTypes
import app.revanced.patcher.writer.ASMWriter.insertAt
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

class OldQualityLayout : Patch("old-quality-restore") {
    override fun execute(cache: Cache): PatchResult {
        val method = cache.methods["old-quality-patch"].findParentMethod(
            Signature(
                "old-quality-patch-method",
                ExtraTypes.Any,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
                arrayOf(),
                arrayOf(
                    Opcodes.ALOAD,
                    Opcodes.GETFIELD,
                    Opcodes.ISTORE,
                    Opcodes.ICONST_3,
                    Opcodes.ISTORE
                )
            )
        ) ?: return PatchResultError("Parent method old-quality-patch-method has not been found")

        method.method.instructions.insertAt(
            0,
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "fi/razerman/youtube/XGlobals",
                "useOldStyleQualitySettings",
                "()Z"
            ),
            VarInsnNode(Opcodes.ISTORE, 1),
            VarInsnNode(Opcodes.ILOAD, 1),
            JumpInsnNode(
                Opcodes.IFNE,
                (method.method.instructions[method.scanData.endIndex + 3] as JumpInsnNode).label
            ),
        )

        return PatchResultSuccess()
    }
}