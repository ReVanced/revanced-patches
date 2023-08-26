package app.revanced.patches.youtube.video.information.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.video.information.annotation.VideoInformationCompatibility
import app.revanced.patches.youtube.video.information.fingerprints.*
import app.revanced.patches.youtube.video.speed.remember.patch.RememberPlaybackSpeedPatch
import app.revanced.patches.youtube.video.videoid.patch.VideoIdPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.util.MethodUtil

@Name("Video information")
@Description("Hooks YouTube to get information about the current playing video.")
@VideoInformationCompatibility
@DependsOn([IntegrationsPatch::class, VideoIdPatch::class])
class VideoInformationPatch : BytecodePatch(
    listOf(
        PlayerInitFingerprint,
        CreateVideoPlayerSeekbarFingerprint,
        PlayerControllerSetTimeReferenceFingerprint,
        OnPlaybackSpeedItemClickFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        with(PlayerInitFingerprint.result!!) {
            playerInitMethod = mutableClass.methods.first { MethodUtil.isConstructor(it) }

            // hook the player controller for use through integrations
            onCreateHook(INTEGRATIONS_CLASS_DESCRIPTOR, "initialize")

            // seek method
            val seekFingerprintResultMethod = SeekFingerprint.also { it.resolve(context, classDef) }.result!!.method

            // create helper method
            val seekHelperMethod = ImmutableMethod(
                seekFingerprintResultMethod.definingClass,
                "seekTo",
                listOf(ImmutableMethodParameter("J", null, "time")),
                "Z",
                AccessFlags.PUBLIC or AccessFlags.FINAL,
                null, null,
                MutableMethodImplementation(4)
            ).toMutable()

            // get enum type for the seek helper method
            val seekSourceEnumType = seekFingerprintResultMethod.parameterTypes[1].toString()

            // insert helper method instructions
            seekHelperMethod.addInstructions(
                0,
                """
                    sget-object v0, $seekSourceEnumType->a:$seekSourceEnumType
                    invoke-virtual {p0, p1, p2, v0}, ${seekFingerprintResultMethod.definingClass}->${seekFingerprintResultMethod.name}(J$seekSourceEnumType)Z
                    move-result p1
                    return p1
                """
            )

            // add the seekTo method to the class for the integrations to call
            mutableClass.methods.add(seekHelperMethod)
        }

        with(CreateVideoPlayerSeekbarFingerprint.result!!) {
            val videoLengthMethodResult = VideoLengthFingerprint.also { it.resolve(context, classDef) }.result!!

            with(videoLengthMethodResult.mutableMethod) {
                val videoLengthRegisterIndex = videoLengthMethodResult.scanResult.patternScanResult!!.endIndex - 2
                val videoLengthRegister = getInstruction<OneRegisterInstruction>(videoLengthRegisterIndex).registerA
                val dummyRegisterForLong = videoLengthRegister + 1 // required for long values since they are wide

                addInstruction(
                    videoLengthMethodResult.scanResult.patternScanResult!!.endIndex,
                    "invoke-static {v$videoLengthRegister, v$dummyRegisterForLong}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoLength(J)V"
                )
            }
        }

        /*
         * Inject call for video id
         */
        val videoIdMethodDescriptor = "$INTEGRATIONS_CLASS_DESCRIPTOR->setVideoId(Ljava/lang/String;)V"
        VideoIdPatch.injectCall(videoIdMethodDescriptor)
        VideoIdPatch.injectCallBackgroundPlay(videoIdMethodDescriptor)

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
        OnPlaybackSpeedItemClickFingerprint.result?.apply {
            speedSelectionInsertMethod = mutableMethod
            speedSelectionInsertIndex = scanResult.patternScanResult!!.startIndex - 3
            speedSelectionValueRegister =
                mutableMethod.getInstruction<FiveRegisterInstruction>(speedSelectionInsertIndex).registerD

            val speedSelectionMethodInstructions = mutableMethod.implementation!!.instructions
            setPlaybackSpeedContainerClassFieldReference =
                getReference(speedSelectionMethodInstructions, -1, Opcode.IF_EQZ)
            setPlaybackSpeedClassFieldReference =
                getReference(speedSelectionMethodInstructions, 1, Opcode.IGET)
            setPlaybackSpeedMethodReference =
                getReference(speedSelectionMethodInstructions, 2, Opcode.IGET)
        } ?: throw OnPlaybackSpeedItemClickFingerprint.exception

        userSelectedPlaybackSpeedHook(INTEGRATIONS_CLASS_DESCRIPTOR, "userSelectedPlaybackSpeed")
    }

    companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/VideoInformation;"

        private lateinit var playerInitMethod: MutableMethod
        private var playerInitInsertIndex = 4

        private lateinit var timeMethod: MutableMethod
        private var timeInitInsertIndex = 2

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

        private fun getReference(instructions: List<BuilderInstruction>, offset: Int, opcode: Opcode) =
            (instructions[instructions.indexOfFirst { it.opcode == opcode } + offset] as ReferenceInstruction)
                .reference.toString()

        private lateinit var speedSelectionInsertMethod: MutableMethod
        private var speedSelectionInsertIndex = 0
        private var speedSelectionValueRegister = 0

        /**
         * Hook the video speed selected by the user.
         */
        internal fun userSelectedPlaybackSpeedHook(targetMethodClass: String, targetMethodName: String) =
            speedSelectionInsertMethod.addInstruction(
            speedSelectionInsertIndex++,
            "invoke-static {v$speedSelectionValueRegister}, $targetMethodClass->$targetMethodName(F)V"
        )
        
        /**
         * Used by [RememberPlaybackSpeedPatch]
         */
        internal lateinit var setPlaybackSpeedContainerClassFieldReference: String
        internal lateinit var setPlaybackSpeedClassFieldReference: String
        internal lateinit var setPlaybackSpeedMethodReference: String
    }
}