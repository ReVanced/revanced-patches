package app.revanced.patches.youtube.video.playerresponse

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.video.playerresponse.fingerprint.PlayerParameterBuilderFingerprint
import java.io.Closeable

@Patch(
    dependencies = [IntegrationsPatch::class],
)
object PlayerResponseMethodHookPatch :
    BytecodePatch(setOf(PlayerParameterBuilderFingerprint)),
    Closeable,
    MutableSet<PlayerResponseMethodHookPatch.Hook> by mutableSetOf() {
    private const val VIDEO_ID_PARAMETER = 1
    private const val VIDEO_IS_OPENING_OR_PLAYING_PARAMETER = 11
    private const val PROTO_BUFFER_PARAMETER_PARAMETER = 3

    private lateinit var playerResponseMethod: MutableMethod

    override fun execute(context: BytecodeContext) {
        playerResponseMethod = PlayerParameterBuilderFingerprint.result?.mutableMethod
            ?: throw PlayerParameterBuilderFingerprint.exception
    }

    override fun close() {
        fun hookVideoId(hook: Hook) = playerResponseMethod.addInstruction(
            0, "invoke-static {p$VIDEO_ID_PARAMETER, p$VIDEO_IS_OPENING_OR_PLAYING_PARAMETER}, $hook"
        )

        fun hookProtoBufferParameter(hook: Hook) = playerResponseMethod.addInstructions(
            0,
            """
                invoke-static {p$PROTO_BUFFER_PARAMETER_PARAMETER}, $hook
                move-result-object p$PROTO_BUFFER_PARAMETER_PARAMETER
            """
        )

        // Reverse the order in order to preserve insertion order of the hooks.
        val beforeVideoIdHooks = filterIsInstance<Hook.ProtoBufferParameterBeforeVideoId>().asReversed()
        val videoIdHooks = filterIsInstance<Hook.VideoId>().asReversed()
        val afterVideoIdHooks = filterIsInstance<Hook.ProtoBufferParameter>().asReversed()

        // Add the hooks in this specific order as they insert instructions at the beginning of the method.
        afterVideoIdHooks.forEach(::hookProtoBufferParameter)
        videoIdHooks.forEach(::hookVideoId)
        beforeVideoIdHooks.forEach(::hookProtoBufferParameter)
    }

    internal abstract class Hook(private val methodDescriptor: String) {
        internal class VideoId(methodDescriptor: String) : Hook(methodDescriptor)

        internal class ProtoBufferParameter(methodDescriptor: String) : Hook(methodDescriptor)
        internal class ProtoBufferParameterBeforeVideoId(methodDescriptor: String) : Hook(methodDescriptor)

        override fun toString() = methodDescriptor
    }
}

