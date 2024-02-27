package app.revanced.patches.music.video.information

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.music.utils.fingerprints.SeekBarConstructorFingerprint
import app.revanced.patches.music.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.video.information.fingerprints.PlayerControllerSetTimeReferenceFingerprint
import app.revanced.patches.music.video.information.fingerprints.VideoEndFingerprint
import app.revanced.patches.music.video.information.fingerprints.VideoLengthFingerprint
import app.revanced.patches.music.video.videoid.VideoIdPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.util.MethodUtil

@Patch(
    dependencies = [
        SharedResourceIdPatch::class,
        VideoIdPatch::class
    ]
)
@Suppress("MemberVisibilityCanBePrivate")
object VideoInformationPatch : BytecodePatch(
    setOf(
        PlayerControllerSetTimeReferenceFingerprint,
        SeekBarConstructorFingerprint,
        VideoEndFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$VIDEO_PATH/VideoInformation;"

    private var playerInitInsertIndex = 4
    private var timeInitInsertIndex = 2

    private lateinit var playerInitMethod: MutableMethod
    private lateinit var timeMethod: MutableMethod

    lateinit var rectangleFieldName: String

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
            }
        } ?: throw VideoEndFingerprint.exception


        /**
         * Set current video length
         */
        SeekBarConstructorFingerprint.result?.classDef?.let { classDef ->
            VideoLengthFingerprint.also {
                it.resolve(
                    context,
                    classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val rectangleReference =
                        getInstruction<ReferenceInstruction>(implementation!!.instructions.count() - 3).reference
                    rectangleFieldName = (rectangleReference as FieldReference).name

                    val videoLengthRegisterIndex = it.scanResult.patternScanResult!!.startIndex + 1
                    val videoLengthRegister =
                        getInstruction<OneRegisterInstruction>(videoLengthRegisterIndex).registerA
                    val dummyRegisterForLong =
                        videoLengthRegister + 1 // required for long values since they are wide

                    addInstruction(
                        videoLengthRegisterIndex + 1,
                        "invoke-static {v$videoLengthRegister, v$dummyRegisterForLong}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoLength(J)V"
                    )
                }
            } ?: throw VideoLengthFingerprint.exception
        } ?: throw SeekBarConstructorFingerprint.exception


        /**
         * Set the video time method
         */
        PlayerControllerSetTimeReferenceFingerprint.result?.let {
            timeMethod = context.toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex, true)
                .getMethod() as MutableMethod
        } ?: throw PlayerControllerSetTimeReferenceFingerprint.exception


        /**
         * Set current video time
         */
        videoTimeHook(INTEGRATIONS_CLASS_DESCRIPTOR, "setVideoTime")


        /**
         * Set current video id
         */
        val videoIdMethodDescriptor = "$INTEGRATIONS_CLASS_DESCRIPTOR->setVideoId(Ljava/lang/String;)V"
        VideoIdPatch.hookVideoId(videoIdMethodDescriptor)
        VideoIdPatch.hookBackgroundPlayVideoId(videoIdMethodDescriptor)
    }
}