package net.revanced.patches.ads

import net.revanced.patcher.cache.Cache
import net.revanced.patcher.patch.Patch
import net.revanced.patcher.patch.PatchResult
import net.revanced.patcher.patch.PatchResultError
import net.revanced.patcher.patch.PatchResultSuccess
import net.revanced.patcher.signature.Signature
import net.revanced.patcher.writer.ASMWriter.insertAt
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

class VideoAds: Patch("VideoAds") {
    override fun execute(cache: Cache): PatchResult {
        val showVideoAdsMethodData = cache.methods["show-video-ads"].findParentMethod(
            Signature(
                "method",
                Type.VOID_TYPE,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
                arrayOf(Type.BOOLEAN_TYPE),
                null
            )
        ) ?: return PatchResultError("Could not find required method to patch")

        showVideoAdsMethodData.method.instructions.insertAt(0,
            VarInsnNode(Opcodes.ISTORE, 1),
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "fi/vanced/libraries/youtube/whitelisting/Whitelist",
                "shouldShowAds",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE)
            )
        )

        return PatchResultSuccess()
    }
}

