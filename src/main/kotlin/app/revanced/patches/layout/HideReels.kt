package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.writer.ASMWriter.insertAt
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

class HideReels : Patch("hide-reels") {
    override fun execute(cache: Cache): PatchResult {
        val patchData = cache.methods["hide-reel-patch"]

        patchData.method.instructions.insertAt(
            patchData.scanData.endIndex + 1,
            VarInsnNode(Opcodes.ALOAD, 18),
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