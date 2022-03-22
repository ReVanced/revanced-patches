package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.writer.ASMWriter.insertAt
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

class CreateButtonRemover : Patch("create-button-remover") {
    override fun execute(cache: Cache): PatchResult {
        val patchData = cache.methods["create-button-patch"]

        patchData.method.instructions.insertAt(
            patchData.scanData.endIndex - 1,
            VarInsnNode(
                Opcodes.ALOAD,
                6
            ),
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "fi/razerman/youtube/XAdRemover",
                "hideCreateButton",
                "(Landroid/view/View;)V"
            )
        )

        return PatchResultSuccess()
    }
}