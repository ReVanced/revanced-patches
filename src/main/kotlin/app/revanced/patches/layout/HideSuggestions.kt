package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.signature.Signature
import app.revanced.patcher.writer.ASMWriter.insertAt
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

class HideSuggestions : Patch("hide-suggestions") {
    override fun execute(cache: Cache): PatchResult {
        val method = cache.methods["hide-suggestions-patch"].findParentMethod(
            Signature(
                "hide-suggestions-method",
                Type.VOID_TYPE,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
                arrayOf(Type.BOOLEAN_TYPE),
                arrayOf(
                    Opcodes.ALOAD,
                    Opcodes.ILOAD,
                    Opcodes.PUTFIELD,
                    Opcodes.ALOAD,
                    Opcodes.GETFIELD
                )
            )
        ) ?: return PatchResultError("Parent method hide-suggestions-method has not been found")

        method.method.instructions.insertAt(
            0,
            VarInsnNode(Opcodes.ILOAD, 1),
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/lang/Boolean",
                "valueOf",
                "(Z)Ljava/lang/Boolean"
            ),
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "fi/razerman/youtube/XAdRemover",
                "HideReels",
                "(Landroid/view/View;)V"
            )
        )

        return PatchResultSuccess()
    }
}