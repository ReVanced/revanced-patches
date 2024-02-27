package app.revanced.patches.youtube.utils.videocpn

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.utils.fingerprints.OrganicPlaybackContextModelFingerprint
import app.revanced.patches.youtube.utils.videoid.general.VideoIdPatch
import app.revanced.util.exception

@Patch(dependencies = [VideoIdPatch::class])
object VideoCpnPatch : BytecodePatch(
    setOf(OrganicPlaybackContextModelFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        insertMethod = OrganicPlaybackContextModelFingerprint.result?.mutableMethod
            ?: throw OrganicPlaybackContextModelFingerprint.exception

    }

    private lateinit var insertMethod: MutableMethod

    internal fun injectCall(
        methodDescriptor: String
    ) {
        insertMethod.addInstructions(
            2,
            "invoke-static {p1}, $methodDescriptor"
        )
    }
}

