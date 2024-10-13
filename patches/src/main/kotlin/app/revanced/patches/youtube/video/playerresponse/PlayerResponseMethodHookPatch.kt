package app.revanced.patches.youtube.video.playerresponse

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch

private val hooks = mutableSetOf<Hook>()

fun addPlayerResponseMethodHook(hook: Hook) {
    hooks += hook
}

// Parameter numbers of the patched method.
private const val PARAMETER_VIDEO_ID = 1
private const val PARAMETER_PROTO_BUFFER = 3
private const val PARAMETER_IS_SHORT_AND_OPENING_OR_PLAYING = 11

// Registers used to pass the parameters to the extension.
private var playerResponseMethodCopyRegisters = false
private lateinit var registerVideoId: String
private lateinit var registerProtoBuffer: String
private lateinit var registerIsShortAndOpeningOrPlaying: String

private lateinit var playerResponseMethod: MutableMethod
private var numberOfInstructionsAdded = 0

val playerResponseMethodHookPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

    val playerParameterBuilderMatch by playerParameterBuilderFingerprint()

    execute {
        playerResponseMethod = playerParameterBuilderMatch.mutableMethod

        // On some app targets the method has too many registers pushing the parameters past v15.
        // If needed, move the parameters to 4-bit registers, so they can be passed to the extension.
        playerResponseMethodCopyRegisters = playerResponseMethod.implementation!!.registerCount -
            playerResponseMethod.parameterTypes.size + PARAMETER_IS_SHORT_AND_OPENING_OR_PLAYING > 15

        if (playerResponseMethodCopyRegisters) {
            registerVideoId = "v0"
            registerProtoBuffer = "v1"
            registerIsShortAndOpeningOrPlaying = "v2"
        } else {
            registerVideoId = "p$PARAMETER_VIDEO_ID"
            registerProtoBuffer = "p$PARAMETER_PROTO_BUFFER"
            registerIsShortAndOpeningOrPlaying = "p$PARAMETER_IS_SHORT_AND_OPENING_OR_PLAYING"
        }
    }

    finalize {
        fun hookVideoId(hook: Hook) {
            playerResponseMethod.addInstruction(
                0,
                "invoke-static {$registerVideoId, $registerIsShortAndOpeningOrPlaying}, $hook",
            )
            numberOfInstructionsAdded++
        }

        fun hookProtoBufferParameter(hook: Hook) {
            playerResponseMethod.addInstructions(
                0,
                """
                    invoke-static {$registerProtoBuffer, $registerVideoId, $registerIsShortAndOpeningOrPlaying}, $hook
                    move-result-object $registerProtoBuffer
            """,
            )
            numberOfInstructionsAdded += 2
        }

        // Reverse the order in order to preserve insertion order of the hooks.
        val beforeVideoIdHooks = hooks.filterIsInstance<Hook.ProtoBufferParameterBeforeVideoId>().asReversed()
        val videoIdHooks = hooks.filterIsInstance<Hook.VideoId>().asReversed()
        val afterVideoIdHooks = hooks.filterIsInstance<Hook.ProtoBufferParameter>().asReversed()

        // Add the hooks in this specific order as they insert instructions at the beginning of the method.
        afterVideoIdHooks.forEach(::hookProtoBufferParameter)
        videoIdHooks.forEach(::hookVideoId)
        beforeVideoIdHooks.forEach(::hookProtoBufferParameter)

        if (playerResponseMethodCopyRegisters) {
            playerResponseMethod.addInstructions(
                0,
                """
                    move-object/from16 $registerVideoId, p$PARAMETER_VIDEO_ID
                    move-object/from16 $registerProtoBuffer, p$PARAMETER_PROTO_BUFFER
                    move/from16        $registerIsShortAndOpeningOrPlaying, p$PARAMETER_IS_SHORT_AND_OPENING_OR_PLAYING
                """,
            )
            numberOfInstructionsAdded += 3

            // Move the modified register back.
            playerResponseMethod.addInstruction(
                numberOfInstructionsAdded,
                "move-object/from16 p$PARAMETER_PROTO_BUFFER, $registerProtoBuffer",
            )
        }
    }
}

sealed class Hook private constructor(private val methodDescriptor: String) {
    class VideoId(methodDescriptor: String) : Hook(methodDescriptor)

    class ProtoBufferParameter(methodDescriptor: String) : Hook(methodDescriptor)
    class ProtoBufferParameterBeforeVideoId(methodDescriptor: String) : Hook(methodDescriptor)

    override fun toString() = methodDescriptor
}
