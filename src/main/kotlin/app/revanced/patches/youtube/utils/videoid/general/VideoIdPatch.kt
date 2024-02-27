package app.revanced.patches.youtube.utils.videoid.general

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.utils.fingerprints.OrganicPlaybackContextModelFingerprint
import app.revanced.patches.youtube.utils.fingerprints.VideoEndFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.playerresponse.PlayerResponsePatch
import app.revanced.patches.youtube.utils.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.utils.videoid.general.fingerprint.PlayerControllerSetTimeReferenceFingerprint
import app.revanced.patches.youtube.utils.videoid.general.fingerprint.VideoIdFingerprint
import app.revanced.patches.youtube.utils.videoid.general.fingerprint.VideoIdParentFingerprint
import app.revanced.patches.youtube.utils.videoid.general.fingerprint.VideoLengthFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.util.MethodUtil

@Patch(
    dependencies = [
        PlayerTypeHookPatch::class,
        PlayerResponsePatch::class
    ]
)
object VideoIdPatch : BytecodePatch(
    setOf(
        OrganicPlaybackContextModelFingerprint,
        PlayerControllerSetTimeReferenceFingerprint,
        VideoEndFingerprint,
        VideoIdParentFingerprint,
        VideoLengthFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        VideoEndFingerprint.result?.let {
            playerInitMethod =
                it.mutableClass.methods.first { method -> MethodUtil.isConstructor(method) }

            // hook the player controller for use through integrations
            onCreateHook(INTEGRATIONS_CLASS_DESCRIPTOR, "initialize")

            it.mutableMethod.apply {
                val seekHelperMethod = ImmutableMethod(
                    definingClass,
                    "seekTo",
                    listOf(ImmutableMethodParameter("J", annotations, "time")),
                    "Z",
                    AccessFlags.PUBLIC or AccessFlags.FINAL,
                    annotations, null,
                    MutableMethodImplementation(4)
                ).toMutable()

                val seekSourceEnumType = parameterTypes[1].toString()

                seekHelperMethod.addInstructions(
                    0, """
                            sget-object v0, $seekSourceEnumType->a:$seekSourceEnumType
                            invoke-virtual {p0, p1, p2, v0}, ${definingClass}->${name}(J$seekSourceEnumType)Z
                            move-result p1
                            return p1
                            """
                )
                it.mutableClass.methods.add(seekHelperMethod)

                val videoEndMethod = context.toMethodWalker(it.method)
                    .nextMethod(it.scanResult.patternScanResult!!.startIndex + 1, true)
                    .getMethod() as MutableMethod

                videoEndMethod.apply {
                    addInstructionsWithLabels(
                        0, """
                            invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->videoEnded()Z
                            move-result v0
                            if-eqz v0, :end
                            return-void
                            """, ExternalLabel("end", getInstruction(0))
                    )
                }
            }
        } ?: throw VideoEndFingerprint.exception

        /**
         * Set current video time
         */
        PlayerControllerSetTimeReferenceFingerprint.result?.let {
            timeMethod = context.toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex, true)
                .getMethod() as MutableMethod
        } ?: throw PlayerControllerSetTimeReferenceFingerprint.exception

        /**
         * Hook the methods which set the time
         */
        videoTimeHook(INTEGRATIONS_CLASS_DESCRIPTOR, "setVideoTime")

        /**
         * Set current video is livestream
         */
        OrganicPlaybackContextModelFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    2,
                    "sput-boolean p2, $INTEGRATIONS_CLASS_DESCRIPTOR->isLiveStream:Z"
                )
            }
        } ?: throw OrganicPlaybackContextModelFingerprint.exception

        /**
         * Set current video length
         */
        VideoLengthFingerprint.result?.let {
            it.mutableMethod.apply {
                val startIndex = it.scanResult.patternScanResult!!.startIndex
                val primaryRegister = getInstruction<OneRegisterInstruction>(startIndex).registerA
                val secondaryRegister = primaryRegister + 1

                addInstruction(
                    startIndex + 2,
                    "invoke-static {v$primaryRegister, v$secondaryRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoLength(J)V"
                )
            }
        } ?: throw VideoLengthFingerprint.exception

        VideoIdParentFingerprint.result?.let { parentResult ->
            VideoIdFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    insertMethod = this
                    insertIndex = it.scanResult.patternScanResult!!.endIndex
                    videoIdRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA
                }
                offset++ // offset so setVideoId is called before any injected call
            } ?: throw VideoIdFingerprint.exception
        } ?: throw VideoIdParentFingerprint.exception

        injectCall("$INTEGRATIONS_CLASS_DESCRIPTOR->setVideoId(Ljava/lang/String;)V")
        injectPlayerResponseVideoId("$INTEGRATIONS_CLASS_DESCRIPTOR->setPlayerResponseVideoId(Ljava/lang/String;Z)V")
        // Call before any other video id hooks,
        // so they can use VideoInformation and check if the video id is for a Short.
        PlayerResponsePatch += PlayerResponsePatch.Hook.PlayerBeforeVideoId(
            "$INTEGRATIONS_CLASS_DESCRIPTOR->newPlayerParameter(Ljava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;"
        )
    }

    const val INTEGRATIONS_CLASS_DESCRIPTOR = "$VIDEO_PATH/VideoInformation;"

    private var offset = 0
    private var playerInitInsertIndex = 4
    private var timeInitInsertIndex = 2

    private var insertIndex: Int = 0
    private var videoIdRegister: Int = 0
    private lateinit var insertMethod: MutableMethod
    private lateinit var playerInitMethod: MutableMethod
    private lateinit var timeMethod: MutableMethod

    /**
     * Adds an invoke-static instruction, called with the new id when the video changes
     * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
     */
    internal fun injectCall(
        methodDescriptor: String
    ) {
        insertMethod.addInstructions(
            insertIndex + offset, // move-result-object offset
            "invoke-static {v$videoIdRegister}, $methodDescriptor"
        )
    }

    /**
     * Hooks the video id of every video when loaded.
     * Supports all videos and functions in all situations.
     *
     * First parameter is the video id.
     * Second parameter is if the video is a Short AND it is being opened or is currently playing.
     *
     * Hook is always called off the main thread.
     *
     * This hook is called as soon as the player response is parsed,
     * and called before many other hooks are updated such as [PlayerTypeHookPatch].
     *
     * Note: The video id returned here may not be the current video that's being played.
     * It's common for multiple Shorts to load at once in preparation
     * for the user swiping to the next Short.
     *
     * Be aware, this can be called multiple times for the same video id.
     *
     * @param methodDescriptor which method to call. Params must be `Ljava/lang/String;Z`
     */
    internal fun injectPlayerResponseVideoId(
        methodDescriptor: String
    ) {
        PlayerResponsePatch += PlayerResponsePatch.Hook.VideoId(
            methodDescriptor
        )
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
            "v0",
            "$targetMethodClass->$targetMethodName(Ljava/lang/Object;)V"
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
}

