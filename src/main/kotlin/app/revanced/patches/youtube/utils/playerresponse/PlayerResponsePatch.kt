package app.revanced.patches.youtube.utils.playerresponse

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.utils.fingerprints.PlayerParameterBuilderFingerprint
import app.revanced.util.exception
import java.io.Closeable

object PlayerResponsePatch : BytecodePatch(
    setOf(PlayerParameterBuilderFingerprint)
), Closeable, MutableSet<PlayerResponsePatch.Hook> by mutableSetOf() {
    private var VIDEO_ID_PARAMETER = 1
    private var PLAYER_PARAMETER = 3
    private var IS_SHORT_AND_OPENING_OR_PLAYING_PARAMETER = 11
    private var freeRegister = 0
    private var shouldApplyNewMethod = false

    private lateinit var playerResponseMethod: MutableMethod

    override fun execute(context: BytecodeContext) {
        playerResponseMethod = PlayerParameterBuilderFingerprint.result?.mutableMethod
            ?: throw PlayerParameterBuilderFingerprint.exception

        playerResponseMethod.apply {
            freeRegister = implementation!!.registerCount - parameters.size - 2
            shouldApplyNewMethod = freeRegister > 2
            if (shouldApplyNewMethod) {
                IS_SHORT_AND_OPENING_OR_PLAYING_PARAMETER = freeRegister
                PLAYER_PARAMETER = freeRegister - 1
                VIDEO_ID_PARAMETER = freeRegister - 2
            }
        }
    }

    override fun close() {
        fun hookVideoId(hook: Hook) {
            playerResponseMethod.apply {
                val instruction =
                    if (shouldApplyNewMethod)
                        "invoke-static {v$VIDEO_ID_PARAMETER, v$IS_SHORT_AND_OPENING_OR_PLAYING_PARAMETER}, $hook"
                    else
                        "invoke-static {p$VIDEO_ID_PARAMETER, p$IS_SHORT_AND_OPENING_OR_PLAYING_PARAMETER}, $hook"
                addInstruction(
                    0,
                    instruction
                )
            }
        }

        fun hookPlayerParameter(hook: Hook) {
            playerResponseMethod.apply {
                val instruction =
                    if (shouldApplyNewMethod)
                        """
                            invoke-static {v$VIDEO_ID_PARAMETER, v$PLAYER_PARAMETER, v$IS_SHORT_AND_OPENING_OR_PLAYING_PARAMETER}, $hook
                            move-result-object p3
                            """
                    else
                        """
                            invoke-static {p$VIDEO_ID_PARAMETER, p$PLAYER_PARAMETER, p$IS_SHORT_AND_OPENING_OR_PLAYING_PARAMETER}, $hook
                            move-result-object p$PLAYER_PARAMETER
                            """
                addInstructions(
                    0,
                    instruction
                )
            }
        }

        // Reverse the order in order to preserve insertion order of the hooks.
        val beforeVideoIdHooks = filterIsInstance<Hook.PlayerBeforeVideoId>().asReversed()
        val videoIdHooks = filterIsInstance<Hook.VideoId>().asReversed()
        val afterVideoIdHooks = filterIsInstance<Hook.PlayerParameter>().asReversed()

        // Add the hooks in this specific order as they insert instructions at the beginning of the method.
        afterVideoIdHooks.forEach(::hookPlayerParameter)
        videoIdHooks.forEach(::hookVideoId)
        beforeVideoIdHooks.forEach(::hookPlayerParameter)

        if (shouldApplyNewMethod) {
            playerResponseMethod.addInstructions(
                0, """
                    move-object v$VIDEO_ID_PARAMETER, p1
                    move-object v$PLAYER_PARAMETER, p3
                    move/from16 v$IS_SHORT_AND_OPENING_OR_PLAYING_PARAMETER, p11
                    """
            )
        }
    }

    internal abstract class Hook(private val methodDescriptor: String) {
        internal class VideoId(methodDescriptor: String) : Hook(methodDescriptor)

        internal class PlayerParameter(methodDescriptor: String) : Hook(methodDescriptor)
        internal class PlayerBeforeVideoId(methodDescriptor: String) : Hook(methodDescriptor)

        override fun toString() = methodDescriptor
    }
}

