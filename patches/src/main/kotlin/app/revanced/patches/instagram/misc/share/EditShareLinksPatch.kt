package app.revanced.patches.instagram.misc.share

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal fun BytecodePatchContext.editShareLinksPatch(block: MutableMethod.(index: Int, register: Int) -> Unit) {
    val methodsToPatch = arrayOf(
        permalinkResponseJsonParserMethodMatch,
        storyUrlResponseJsonParserMethodMatch,
        profileUrlResponseJsonParserMethodMatch,
        liveUrlResponseJsonParserMethodMatch,
    )

    methodsToPatch.forEach { match ->
        try {
            match.method.apply {
                val putSharingUrlIndex = indexOfFirstInstruction(
                    match[0],
                    Opcode.IPUT_OBJECT
                )
                val sharingUrlRegister = getInstruction<TwoRegisterInstruction>(putSharingUrlIndex).registerA
                block(putSharingUrlIndex, sharingUrlRegister)
            }
        } catch (e: java.lang.IllegalArgumentException) {
            java.util.logging.Logger.getLogger("EditShareLinksPatch").warning("Failed to find IPUT_OBJECT in share link method: ${e.message}")
        } catch (e: java.lang.Exception) {
            java.util.logging.Logger.getLogger("EditShareLinksPatch").warning("Error patching share link method: ${e.message}")
        }
    }
}
