package app.revanced.patches.music.utils.playerresponse

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.music.utils.playerresponse.fingerprints.PlaybackStartDescriptorFingerprint
import app.revanced.util.exception

object PlayerResponsePatch : BytecodePatch(
    setOf(PlaybackStartDescriptorFingerprint)
) {
    private const val VIDEO_ID_PARAMETER = 1
    private const val PLAYLIST_ID_PARAMETER = 4
    private const val PLAYLIST_INDEX_PARAMETER = 5
    private const val VIDEO_IS_OPENING_OR_PLAYING_PARAMETER = 12

    private lateinit var insertMethod: MutableMethod

    internal fun injectCall(
        methodDescriptor: String
    ) {
        insertMethod.addInstructions(
            0, // move-result-object offset
            "invoke-static {p$VIDEO_ID_PARAMETER, p$VIDEO_IS_OPENING_OR_PLAYING_PARAMETER}, $methodDescriptor"
        )
    }

    internal fun injectPlaylistCall(
        methodDescriptor: String
    ) {
        insertMethod.addInstructions(
            0, // move-result-object offset
            "invoke-static {p$VIDEO_ID_PARAMETER, p$PLAYLIST_ID_PARAMETER, p$PLAYLIST_INDEX_PARAMETER, p$VIDEO_IS_OPENING_OR_PLAYING_PARAMETER}, $methodDescriptor"
        )
    }

    override fun execute(context: BytecodeContext) {

        PlaybackStartDescriptorFingerprint.result?.let {
            insertMethod = it.mutableMethod
        } ?: throw PlaybackStartDescriptorFingerprint.exception

    }

}

