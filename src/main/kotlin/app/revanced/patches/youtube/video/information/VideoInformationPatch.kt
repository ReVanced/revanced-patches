package app.revanced.patches.youtube.video.information

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.video.information.fingerprints.*
import app.revanced.patches.youtube.video.playerresponse.PlayerResponseMethodHookPatch
import app.revanced.patches.youtube.video.videoid.VideoIdPatch
import app.revanced.util.alsoResolve
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.util.MethodUtil
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    description = "Hooks YouTube to get information about the current playing video.",
    dependencies = [IntegrationsPatch::class, VideoIdPatch::class, PlayerResponseMethodHookPatch::class]
)
object VideoInformationPatch : BytecodePatch(
    setOf(
        PlayerInitFingerprint,
        MdxPlayerDirectorSetVideoStageFingerprint,
        CreateVideoPlayerSeekbarFingerprint,
        PlayerControllerSetTimeReferenceFingerprint,
        OnPlaybackSpeedItemClickFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/VideoInformation;"
    private const val INTEGRATIONS_PLAYER_INTERFACE = "Lapp/revanced/integrations/youtube/patches/VideoInformation${'$'}PlaybackController;"

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
    internal lateinit var setPlaybackSpeedContainerClassFieldReference: String
    internal lateinit var setPlaybackSpeedClassFieldReference: String
    internal lateinit var setPlaybackSpeedMethodReference: String

    override fun execute(context: BytecodeContext) {

        with(PlayerInitFingerprint.resultOrThrow()) {
            playerInitMethod = mutableClass.methods.first { MethodUtil.isConstructor(it) }

            // find the location of the first invoke-direct call and extract the register storing the 'this' object reference.
            val initThisIndex = playerInitMethod.indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_DIRECT && getReference<MethodReference>()?.name == "<init>"
            }
            playerInitInsertRegister = playerInitMethod.getInstruction<FiveRegisterInstruction>(initThisIndex).registerC
            playerInitInsertIndex = initThisIndex + 1

            // Hook the player controller for use through integrations.
            onCreateHook(INTEGRATIONS_CLASS_DESCRIPTOR, "initialize")

            val seekFingerprintResultMethod =
                SeekFingerprint.alsoResolve(context, PlayerInitFingerprint).method
            val seekRelativeFingerprintResultMethod =
                SeekRelativeFingerprint.alsoResolve(context, PlayerInitFingerprint).method

            // Create integrations interface methods.
            addSeekInterfaceMethods(mutableClass, seekFingerprintResultMethod, seekRelativeFingerprintResultMethod)
        }

        with(MdxPlayerDirectorSetVideoStageFingerprint.resultOrThrow()) {
            mdxInitMethod = mutableClass.methods.first { MethodUtil.isConstructor(it) }

            val initThisIndex = mdxInitMethod.indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_DIRECT && getReference<MethodReference>()?.name == "<init>"
            }
            mdxInitInsertRegister = mdxInitMethod.getInstruction<FiveRegisterInstruction>(initThisIndex).registerC
            mdxInitInsertIndex = initThisIndex + 1

            // Hook the MDX director for use through integrations.
            onCreateHookMdx(INTEGRATIONS_CLASS_DESCRIPTOR, "initializeMdx")

            val mdxSeekFingerprintResultMethod =
                MdxSeekFingerprint.alsoResolve(context, MdxPlayerDirectorSetVideoStageFingerprint).method
            val mdxSeekRelativeFingerprintResultMethod =
                MdxSeekRelativeFingerprint.alsoResolve(context, MdxPlayerDirectorSetVideoStageFingerprint).method

            addSeekInterfaceMethods(mutableClass, mdxSeekFingerprintResultMethod, mdxSeekRelativeFingerprintResultMethod)
        }

        with(CreateVideoPlayerSeekbarFingerprint.result!!) {
            val videoLengthMethodResult = VideoLengthFingerprint.also { it.resolve(context, classDef) }.result!!

            with(videoLengthMethodResult.mutableMethod) {
                val videoLengthRegisterIndex = videoLengthMethodResult.scanResult.patternScanResult!!.endIndex - 2
                val videoLengthRegister = getInstruction<OneRegisterInstruction>(videoLengthRegisterIndex).registerA
                val dummyRegisterForLong = videoLengthRegister + 1 // required for long values since they are wide

                addInstruction(
                    videoLengthMethodResult.scanResult.patternScanResult!!.endIndex,
                    "invoke-static { v$videoLengthRegister, v$dummyRegisterForLong }, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoLength(J)V"
                )
            }
        }

        /*
         * Inject call for video ids
         */
        val videoIdMethodDescriptor = "$INTEGRATIONS_CLASS_DESCRIPTOR->setVideoId(Ljava/lang/String;)V"
        VideoIdPatch.hookVideoId(videoIdMethodDescriptor)
        VideoIdPatch.hookBackgroundPlayVideoId(videoIdMethodDescriptor)
        VideoIdPatch.hookPlayerResponseVideoId(
            "$INTEGRATIONS_CLASS_DESCRIPTOR->setPlayerResponseVideoId(Ljava/lang/String;Z)V")
        // Call before any other video id hooks,
        // so they can use VideoInformation and check if the video id is for a Short.
        PlayerResponseMethodHookPatch += PlayerResponseMethodHookPatch.Hook.ProtoBufferParameterBeforeVideoId(
            "$INTEGRATIONS_CLASS_DESCRIPTOR->newPlayerResponseSignature(Ljava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;")

        /*
         * Set the video time method
         */
        with(PlayerControllerSetTimeReferenceFingerprint.result!!) {
            timeMethod = context.toMethodWalker(method)
                .nextMethod(scanResult.patternScanResult!!.startIndex, true)
                .getMethod() as MutableMethod
        }

        /*
         * Hook the methods which set the time
         */
        videoTimeHook(INTEGRATIONS_CLASS_DESCRIPTOR, "setVideoTime")

        /*
         * Hook the user playback speed selection
         */
        OnPlaybackSpeedItemClickFingerprint.result?.mutableMethod?.apply {
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
        } ?: throw OnPlaybackSpeedItemClickFingerprint.exception

        userSelectedPlaybackSpeedHook(INTEGRATIONS_CLASS_DESCRIPTOR, "userSelectedPlaybackSpeed")
    }

    private fun addSeekInterfaceMethods(targetClass: MutableClass, seekToMethod: Method, seekToRelativeMethod: Method) {
        // Add the interface and methods that integrations calls.
        targetClass.interfaces.add(INTEGRATIONS_PLAYER_INTERFACE)

        arrayOf(
            Triple(seekToMethod, "seekTo", true),
            Triple(seekToRelativeMethod, "seekToRelative", false)
        ).forEach { (method, name, returnsBoolean) ->
            // Add interface method.
            // Get enum type for the seek helper method.
            val seekSourceEnumType = method.parameterTypes[1].toString()

            val interfaceImplementation = ImmutableMethod(
                targetClass.type,
                name,
                listOf(ImmutableMethodParameter("J", null, "time")),
                if (returnsBoolean) "Z" else "V",
                AccessFlags.PUBLIC or AccessFlags.FINAL,
                null, null,
                MutableMethodImplementation(4)
            ).toMutable()

            var instructions = """
                    # first enum (field a) is SEEK_SOURCE_UNKNOWN
                    sget-object v0, $seekSourceEnumType->a:$seekSourceEnumType
                    invoke-virtual { p0, p1, p2, v0 }, $method
                """

            instructions += if (returnsBoolean) """
                move-result p1
                return p1                
            """ else """
                return-void                
            """

            // Insert helper method instructions.
            interfaceImplementation.addInstructions(
                0,
                instructions
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
            "$targetMethodClass->$targetMethodName($INTEGRATIONS_PLAYER_INTERFACE)V"
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
            "$targetMethodClass->$targetMethodName($INTEGRATIONS_PLAYER_INTERFACE)V"
        )

    /**
     * Hook the video time.
     * The hook is usually called once per second.
     *
     * @param targetMethodClass The descriptor for the static method to invoke when the player controller is created.
     * @param targetMethodName The name of the static method to invoke when the player controller is created.
     */
    internal fun videoTimeHook(targetMethodClass: String, targetMethodName: String) =
        timeMethod.insertTimeHook(
            timeInitInsertIndex++,
            "$targetMethodClass->$targetMethodName(J)V"
        )

    private fun getReference(instructions: List<BuilderInstruction>, offset: Int, opcode: Opcode) =
        (instructions[instructions.indexOfFirst { it.opcode == opcode } + offset] as ReferenceInstruction)
            .reference.toString()

    /**
     * Hook the video speed selected by the user.
     */
    internal fun userSelectedPlaybackSpeedHook(targetMethodClass: String, targetMethodName: String) =
        speedSelectionInsertMethod.addInstruction(
            speedSelectionInsertIndex++,
            "invoke-static {v$speedSelectionValueRegister}, $targetMethodClass->$targetMethodName(F)V"
        )
}