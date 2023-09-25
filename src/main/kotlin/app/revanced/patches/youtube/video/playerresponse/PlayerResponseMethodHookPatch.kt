package app.revanced.patches.youtube.video.playerresponse

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.fix.playback.SpoofSignaturePatch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.video.playerresponse.fingerprint.PlayerParameterBuilderFingerprint
import app.revanced.patches.youtube.video.videoid.VideoIdPatch

@Patch(
    dependencies = [IntegrationsPatch::class],
)
object PlayerResponseMethodHookPatch : BytecodePatch(
    setOf(
        PlayerParameterBuilderFingerprint,
    )
) {
    private const val playerResponseVideoIdParameter = 1
    private const val playerResponseProtoBufferParameter = 3
    /**
     * Insert index when adding a video id hook.
     */
    private var playerResponseVideoIdInsertIndex = 0
    /**
     * Insert index when adding a proto buffer override.
     * Must be after all video id hooks in the same method.
     */
    private var playerResponseProtoBufferInsertIndex = 0
    private lateinit var playerResponseMethod: MutableMethod

    override fun execute(context: BytecodeContext) {

        // Hook player parameter.
        PlayerParameterBuilderFingerprint.result?.let {
            playerResponseMethod = it.mutableMethod
        } ?: throw PlayerParameterBuilderFingerprint.exception
    }

    /**
     * Modify the player parameter proto buffer value.
     * Used exclusively by [SpoofSignaturePatch].
     */
    fun injectProtoBufferHook(methodDescriptor: String) {
        playerResponseMethod.addInstructions(
            playerResponseProtoBufferInsertIndex,
            """
               invoke-static {p$playerResponseProtoBufferParameter}, $methodDescriptor
               move-result-object p$playerResponseProtoBufferParameter
            """
        )
        playerResponseProtoBufferInsertIndex += 2
    }

    /**
     * Used by [VideoIdPatch].
     */
    internal fun injectVideoIdHook(methodDescriptor: String) {
        playerResponseMethod.addInstruction(
            // Keep injection calls in the order they're added,
            // and all video id hooks run before proto buffer hooks.
            playerResponseVideoIdInsertIndex++,
            "invoke-static {p$playerResponseVideoIdParameter}, $methodDescriptor"
        )
        playerResponseProtoBufferInsertIndex++
    }
}

