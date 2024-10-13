package app.revanced.patches.youtube.video.information

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.video.playerresponse.Hook
import app.revanced.patches.youtube.video.playerresponse.addPlayerResponseMethodHook
import app.revanced.patches.youtube.video.playerresponse.playerResponseMethodHookPatch
import app.revanced.patches.youtube.video.videoid.hookBackgroundPlayVideoId
import app.revanced.patches.youtube.video.videoid.hookPlayerResponseVideoId
import app.revanced.patches.youtube.video.videoid.hookVideoId
import app.revanced.patches.youtube.video.videoid.videoIdPatch
import app.revanced.util.applyMatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.util.MethodUtil

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/VideoInformation;"
private const val EXTENSION_PLAYER_INTERFACE = "Lapp/revanced/extension/youtube/patches/VideoInformation${'$'}PlaybackController;"

private lateinit var playerInitMethod: MutableMethod
private var playerInitInsertIndex = -1
private var playerInitInsertRegister = -1

private lateinit var mdxInitMethod: MutableMethod
private var mdxInitInsertIndex = -1
private var mdxInitInsertRegister = -1

private lateinit var timeMethod: MutableMethod
private var timeInitInsertIndex = 2

private lateinit var speedSelectionInsertMethod: MutableMethod
private var speedSelectionInsertIndex = -1
private var speedSelectionValueRegister = -1

// Used by other patches.
lateinit var setPlaybackSpeedContainerClassFieldReference: String
    private set
lateinit var setPlaybackSpeedClassFieldReference: String
    private set
lateinit var setPlaybackSpeedMethodReference: String
    private set

val videoInformationPatch = bytecodePatch(
    description = "Hooks YouTube to get information about the current playing video.",
) {
    dependsOn(
        sharedExtensionPatch,
        videoIdPatch,
        playerResponseMethodHookPatch,
    )

    val playerInitMatch by playerInitFingerprint()
    val mdxPlayerDirectorSetVideoStageMatch by mdxPlayerDirectorSetVideoStageFingerprint()
    val createVideoPlayerSeekbarMatch by createVideoPlayerSeekbarFingerprint()
    val playerControllerSetTimeReferenceMatch by playerControllerSetTimeReferenceFingerprint()
    val onPlaybackSpeedItemClickMatch by onPlaybackSpeedItemClickFingerprint()

    execute { context ->
        playerInitMethod = playerInitMatch.mutableClass.methods.first { MethodUtil.isConstructor(it) }

        // Find the location of the first invoke-direct call and extract the register storing the 'this' object reference.
        val initThisIndex = playerInitMethod.indexOfFirstInstructionOrThrow {
            opcode == Opcode.INVOKE_DIRECT && getReference<MethodReference>()?.name == "<init>"
        }
        playerInitInsertRegister = playerInitMethod.getInstruction<FiveRegisterInstruction>(initThisIndex).registerC
        playerInitInsertIndex = initThisIndex + 1

        // Hook the player controller for use through the extension.
        onCreateHook(EXTENSION_CLASS_DESCRIPTOR, "initialize")

        val seekFingerprintResultMethod = seekFingerprint.applyMatch(context, playerInitMatch).method
        val seekRelativeFingerprintResultMethod = seekRelativeFingerprint.applyMatch(context, playerInitMatch).method

        // Create extension interface methods.
        addSeekInterfaceMethods(playerInitMatch.mutableClass, seekFingerprintResultMethod, seekRelativeFingerprintResultMethod)

        with(mdxPlayerDirectorSetVideoStageMatch) {
            mdxInitMethod = mutableClass.methods.first { MethodUtil.isConstructor(it) }

            val initThisIndex = mdxInitMethod.indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_DIRECT && getReference<MethodReference>()?.name == "<init>"
            }
            mdxInitInsertRegister = mdxInitMethod.getInstruction<FiveRegisterInstruction>(initThisIndex).registerC
            mdxInitInsertIndex = initThisIndex + 1

            // Hook the MDX director for use through the extension.
            onCreateHookMdx(EXTENSION_CLASS_DESCRIPTOR, "initializeMdx")

            val mdxSeekFingerprintResultMethod =
                mdxSeekFingerprint.applyMatch(context, mdxPlayerDirectorSetVideoStageMatch).method
            val mdxSeekRelativeFingerprintResultMethod =
                mdxSeekRelativeFingerprint.applyMatch(context, mdxPlayerDirectorSetVideoStageMatch).method

            addSeekInterfaceMethods(mutableClass, mdxSeekFingerprintResultMethod, mdxSeekRelativeFingerprintResultMethod)
        }

        with(createVideoPlayerSeekbarMatch) {
            val videoLengthMethodMatch = videoLengthFingerprint.apply { match(context, classDef) }.match!!

            with(videoLengthMethodMatch.mutableMethod) {
                val videoLengthRegisterIndex = videoLengthMethodMatch.patternMatch!!.endIndex - 2
                val videoLengthRegister = getInstruction<OneRegisterInstruction>(videoLengthRegisterIndex).registerA
                val dummyRegisterForLong = videoLengthRegister + 1 // required for long values since they are wide

                addInstruction(
                    videoLengthMethodMatch.patternMatch!!.endIndex,
                    "invoke-static {v$videoLengthRegister, v$dummyRegisterForLong}, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->setVideoLength(J)V",
                )
            }
        }

        /*
         * Inject call for video ids
         */
        val videoIdMethodDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->setVideoId(Ljava/lang/String;)V"
        hookVideoId(videoIdMethodDescriptor)
        hookBackgroundPlayVideoId(videoIdMethodDescriptor)
        hookPlayerResponseVideoId(
            "$EXTENSION_CLASS_DESCRIPTOR->setPlayerResponseVideoId(Ljava/lang/String;Z)V",
        )
        // Call before any other video id hooks,
        // so they can use VideoInformation and check if the video id is for a Short.
        addPlayerResponseMethodHook(
            Hook.ProtoBufferParameterBeforeVideoId(
                "$EXTENSION_CLASS_DESCRIPTOR->" +
                    "newPlayerResponseSignature(Ljava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;",
            ),
        )

        /*
         * Set the video time method
         */
        timeMethod = context.navigate(playerControllerSetTimeReferenceMatch.method)
            .at(playerControllerSetTimeReferenceMatch.patternMatch!!.startIndex)
            .mutable()

        /*
         * Hook the methods which set the time
         */
        videoTimeHook(EXTENSION_CLASS_DESCRIPTOR, "setVideoTime")

        /*
         * Hook the user playback speed selection
         */
        onPlaybackSpeedItemClickMatch.mutableMethod.apply {
            speedSelectionInsertMethod = this
            val speedSelectionMethodInstructions = this.implementation!!.instructions
            val speedSelectionValueInstructionIndex = speedSelectionMethodInstructions.indexOfFirst {
                it.opcode == Opcode.IGET
            }
            speedSelectionValueRegister =
                getInstruction<TwoRegisterInstruction>(speedSelectionValueInstructionIndex).registerA
            setPlaybackSpeedClassFieldReference =
                getInstruction<ReferenceInstruction>(speedSelectionValueInstructionIndex + 1).reference.toString()
            setPlaybackSpeedMethodReference =
                getInstruction<ReferenceInstruction>(speedSelectionValueInstructionIndex + 2).reference.toString()
            setPlaybackSpeedContainerClassFieldReference =
                getReference(speedSelectionMethodInstructions, -1, Opcode.IF_EQZ)
            speedSelectionInsertIndex = speedSelectionValueInstructionIndex + 1
        }

        userSelectedPlaybackSpeedHook(EXTENSION_CLASS_DESCRIPTOR, "userSelectedPlaybackSpeed")
    }
}
private fun addSeekInterfaceMethods(targetClass: MutableClass, seekToMethod: Method, seekToRelativeMethod: Method) {
    // Add the interface and methods that the extension calls.
    targetClass.interfaces.add(EXTENSION_PLAYER_INTERFACE)

    arrayOf(
        seekToMethod to "seekTo",
        seekToRelativeMethod to "seekToRelative",
    ).forEach { (method, name) ->
        // Add interface method.
        // Get enum type for the seek helper method.
        val seekSourceEnumType = method.parameterTypes[1].toString()

        val interfaceImplementation = ImmutableMethod(
            targetClass.type,
            name,
            listOf(ImmutableMethodParameter("J", null, "time")),
            "Z",
            AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
            null,
            null,
            MutableMethodImplementation(4),
        ).toMutable()

        // Insert helper method instructions.
        interfaceImplementation.addInstructions(
            0,
            """
                    # first enum (field a) is SEEK_SOURCE_UNKNOWN
                    sget-object v0, $seekSourceEnumType->a:$seekSourceEnumType
                    invoke-virtual { p0, p1, p2, v0 }, $method
                    move-result p1
                    return p1
                """,
        )

        targetClass.methods.add(interfaceImplementation)
    }
}

private fun MutableMethod.insert(insertIndex: Int, register: String, descriptor: String) =
    addInstruction(insertIndex, "invoke-static { $register }, $descriptor")

private fun MutableMethod.insertTimeHook(insertIndex: Int, descriptor: String) =
    insert(insertIndex, "p1, p2", descriptor)

/**
 * Hook the player controller.  Called when a video is opened or the current video is changed.
 *
 * Note: This hook is called very early and is called before the video id, video time, video length,
 * and many other data fields are set.
 *
 * @param targetMethodClass The descriptor for the class to invoke when the player controller is created.
 * @param targetMethodName The name of the static method to invoke when the player controller is created.
 */
internal fun onCreateHook(targetMethodClass: String, targetMethodName: String) =
    playerInitMethod.insert(
        playerInitInsertIndex++,
        "v$playerInitInsertRegister",
        "$targetMethodClass->$targetMethodName($EXTENSION_PLAYER_INTERFACE)V",
    )

/**
 * Hook the MDX player director. Called when playing videos while casting to a big screen device.
 *
 * @param targetMethodClass The descriptor for the class to invoke when the player controller is created.
 * @param targetMethodName The name of the static method to invoke when the player controller is created.
 */
internal fun onCreateHookMdx(targetMethodClass: String, targetMethodName: String) =
    mdxInitMethod.insert(
        mdxInitInsertIndex++,
        "v$mdxInitInsertRegister",
        "$targetMethodClass->$targetMethodName($EXTENSION_PLAYER_INTERFACE)V",
    )

/**
 * Hook the video time.
 * The hook is usually called once per second.
 *
 * @param targetMethodClass The descriptor for the static method to invoke when the player controller is created.
 * @param targetMethodName The name of the static method to invoke when the player controller is created.
 */
fun videoTimeHook(targetMethodClass: String, targetMethodName: String) =
    timeMethod.insertTimeHook(
        timeInitInsertIndex++,
        "$targetMethodClass->$targetMethodName(J)V",
    )

private fun getReference(instructions: List<BuilderInstruction>, offset: Int, opcode: Opcode) =
    (instructions[instructions.indexOfFirst { it.opcode == opcode } + offset] as ReferenceInstruction)
        .reference.toString()

/**
 * Hook the video speed selected by the user.
 */
fun userSelectedPlaybackSpeedHook(targetMethodClass: String, targetMethodName: String) =
    speedSelectionInsertMethod.addInstruction(
        speedSelectionInsertIndex++,
        "invoke-static {v$speedSelectionValueRegister}, $targetMethodClass->$targetMethodName(F)V",
    )
