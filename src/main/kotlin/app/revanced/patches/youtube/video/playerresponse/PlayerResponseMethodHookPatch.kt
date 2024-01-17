package app.revanced.patches.youtube.video.playerresponse

import app.revanced.util.exception
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

    // Parameter numbers of the patched method.
    private const val PARAMETER_VIDEO_ID = 1
    private const val PARAMETER_PROTO_BUFFER = 3
    private const val PARAMETER_IS_SHORT_AND_OPENING_OR_PLAYING = 11

    // Temporary 4-bit registers used to pass the parameters to integrations.
    private const val REGISTER_VIDEO_ID = 0
    private const val REGISTER_PROTO_BUFFER = 1
    private const val REGISTER_IS_SHORT_AND_OPENING_OR_PLAYING = 2

    private lateinit var playerResponseMethod: MutableMethod

    private var numberOfInstructionsAdded = 0

    override fun execute(context: BytecodeContext) {
        playerResponseMethod = PlayerParameterBuilderFingerprint.result?.mutableMethod
            ?: throw PlayerParameterBuilderFingerprint.exception
    }

    override fun close() {
        fun hookVideoId(hook: Hook) {
            playerResponseMethod.addInstruction(
                0, "invoke-static {v$REGISTER_VIDEO_ID, v$REGISTER_IS_SHORT_AND_OPENING_OR_PLAYING}, $hook"
            )
            numberOfInstructionsAdded++
        }

        fun hookProtoBufferParameter(hook: Hook) {
            playerResponseMethod.addInstructions(
                0,
                """
                    invoke-static {v$REGISTER_PROTO_BUFFER, v$REGISTER_IS_SHORT_AND_OPENING_OR_PLAYING}, $hook
                    move-result-object v$REGISTER_PROTO_BUFFER
            """
            )
            numberOfInstructionsAdded += 2
        }

        // Reverse the order in order to preserve insertion order of the hooks.
        val beforeVideoIdHooks = filterIsInstance<Hook.ProtoBufferParameterBeforeVideoId>().asReversed()
        val videoIdHooks = filterIsInstance<Hook.VideoId>().asReversed()
        val afterVideoIdHooks = filterIsInstance<Hook.ProtoBufferParameter>().asReversed()

        // Add the hooks in this specific order as they insert instructions at the beginning of the method.
        afterVideoIdHooks.forEach(::hookProtoBufferParameter)
        videoIdHooks.forEach(::hookVideoId)
        beforeVideoIdHooks.forEach(::hookProtoBufferParameter)

        // On some app targets the method has too many registers pushing the parameters past v15.
        // Move the parameters to 4-bit registers so they can be passed to integrations.
        playerResponseMethod.addInstructions(
            0, """
                move-object/from16 v$REGISTER_VIDEO_ID, p$PARAMETER_VIDEO_ID
                move-object/from16 v$REGISTER_PROTO_BUFFER, p$PARAMETER_PROTO_BUFFER
                move/from16        v$REGISTER_IS_SHORT_AND_OPENING_OR_PLAYING, p$PARAMETER_IS_SHORT_AND_OPENING_OR_PLAYING
            """,
        )
        numberOfInstructionsAdded += 3

        // Move the modified register back.
        playerResponseMethod.addInstruction(
            numberOfInstructionsAdded,
            "move-object/from16 p$PARAMETER_PROTO_BUFFER, v$REGISTER_PROTO_BUFFER"
        )
    }

    internal abstract class Hook(private val methodDescriptor: String) {
        internal class VideoId(methodDescriptor: String) : Hook(methodDescriptor)

        internal class ProtoBufferParameter(methodDescriptor: String) : Hook(methodDescriptor)
        internal class ProtoBufferParameterBeforeVideoId(methodDescriptor: String) : Hook(methodDescriptor)

        override fun toString() = methodDescriptor
    }
}

