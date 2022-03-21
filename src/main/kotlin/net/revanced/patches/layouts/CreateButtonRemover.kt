package net.revanced.patches.layouts

import net.revanced.patcher.cache.Cache
import net.revanced.patcher.patch.Patch
import net.revanced.patcher.patch.PatchResult
import net.revanced.patcher.patch.PatchResultSuccess
import net.revanced.patcher.writer.ASMWriter.insertAt
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