package app.revanced.patches.youtube.misc.playercontroller.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playercontroller.annotation.PlayerControllerCompatibility
import app.revanced.patches.youtube.misc.playercontroller.fingerprint.CreateVideoPlayerSeekbarFingerprint
import app.revanced.patches.youtube.misc.playercontroller.fingerprint.PlayerInitFingerprint
import app.revanced.patches.youtube.misc.playercontroller.fingerprint.SeekFingerprint
import app.revanced.patches.youtube.misc.playercontroller.fingerprint.VideoLengthFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodParameter
import org.jf.dexlib2.util.MethodUtil

@Name("player-controller-hook")
@Description("Hooks the player controller")
@PlayerControllerCompatibility
@Version("0.0.1")
@DependsOn([IntegrationsPatch::class])
class PlayerControllerPatch : BytecodePatch(
    listOf(
        PlayerInitFingerprint,
        CreateVideoPlayerSeekbarFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        with(PlayerInitFingerprint.result!!) {
            playerInitMethod = mutableClass.methods.first { MethodUtil.isConstructor(it) }

            // hook the player controller for use through integrations
            onCreateHook(INTEGRATIONS_CLASS_DESCRIPTOR, "playerController_onCreateHook")

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
                val videoLengthRegister = (instruction(videoLengthRegisterIndex) as OneRegisterInstruction).registerA
                val dummyRegisterForLong = videoLengthRegister + 1 // required for long values since they are wide

                addInstruction(
                    videoLengthMethodResult.scanResult.patternScanResult!!.endIndex,
                    "invoke-static {v$videoLengthRegister, v$dummyRegisterForLong}, $INTEGRATIONS_CLASS_DESCRIPTOR->setCurrentVideoLength(J)V"
                )
            }
        }

        return PatchResultSuccess()
    }

    companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/PlayerControllerPatch;"
        private const val INSERT_INDEX = 4

        private lateinit var playerInitMethod: MutableMethod

        /**
         * Hook the player controller.
         *
         * @param targetMethodClass The descriptor for the static method to invoke when the player controller is created.
         */
        internal fun onCreateHook(targetMethodClass: String, targetMethodName: String) =
            playerInitMethod.addInstruction(
                INSERT_INDEX,
                "invoke-static { v0 }, $targetMethodClass->$targetMethodName(Ljava/lang/Object;)V"
            )
    }
}