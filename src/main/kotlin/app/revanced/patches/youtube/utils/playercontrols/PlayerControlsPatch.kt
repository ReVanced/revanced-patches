package app.revanced.patches.youtube.utils.playercontrols

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.utils.fingerprints.PlayerControlsVisibilityModelFingerprint
import app.revanced.patches.youtube.utils.fingerprints.ThumbnailPreviewConfigFingerprint
import app.revanced.patches.youtube.utils.fingerprints.YouTubeControlsOverlayFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.BottomControlsInflateFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.ControlsLayoutInflateFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.FullscreenEngagementSpeedEduVisibleFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.FullscreenEngagementSpeedEduVisibleParentFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.PlayerControlsVisibilityFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.QuickSeekVisibleFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.SeekEDUVisibleFingerprint
import app.revanced.patches.youtube.utils.playercontrols.fingerprints.UserScrubbingFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.Reference

@Patch(dependencies = [SharedResourceIdPatch::class])
object PlayerControlsPatch : BytecodePatch(
    setOf(
        BottomControlsInflateFingerprint,
        ControlsLayoutInflateFingerprint,
        FullscreenEngagementSpeedEduVisibleParentFingerprint,
        PlayerControlsVisibilityModelFingerprint,
        ThumbnailPreviewConfigFingerprint,
        YouTubeControlsOverlayFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        fun MutableMethod.findReference(targetString: String): Reference {
            val targetIndex = getStringInstructionIndex(targetString) + 2
            val targetOpcode = getInstruction(targetIndex).opcode

            if (targetOpcode == Opcode.INVOKE_VIRTUAL) {
                val targetRegister = getInstruction<Instruction35c>(targetIndex).registerD

                val instructions = implementation!!.instructions
                for ((index, instruction) in instructions.withIndex()) {
                    if (instruction.opcode != Opcode.IGET_BOOLEAN) continue

                    if (getInstruction<TwoRegisterInstruction>(index).registerA == targetRegister)
                        return getInstruction<ReferenceInstruction>(index).reference
                }
            } else if (targetOpcode == Opcode.IGET_BOOLEAN) {
                return getInstruction<ReferenceInstruction>(targetIndex).reference
            }

            throw PatchException("Reference not found: $targetString")
        }

        PlayerControlsVisibilityModelFingerprint.result?.classDef?.let { classDef ->
            quickSeekVisibleMutableMethod =
                QuickSeekVisibleFingerprint.also {
                    it.resolve(
                        context,
                        classDef
                    )
                }.result?.mutableMethod ?: throw QuickSeekVisibleFingerprint.exception

            seekEDUVisibleMutableMethod =
                SeekEDUVisibleFingerprint.also {
                    it.resolve(
                        context,
                        classDef
                    )
                }.result?.mutableMethod ?: throw SeekEDUVisibleFingerprint.exception

            userScrubbingMutableMethod =
                UserScrubbingFingerprint.also {
                    it.resolve(
                        context,
                        classDef
                    )
                }.result?.mutableMethod ?: throw UserScrubbingFingerprint.exception
        } ?: throw PlayerControlsVisibilityModelFingerprint.exception

        YouTubeControlsOverlayFingerprint.result?.classDef?.let { classDef ->
            playerControlsVisibilityMutableMethod =
                PlayerControlsVisibilityFingerprint.also {
                    it.resolve(
                        context,
                        classDef
                    )
                }.result?.mutableMethod
                    ?: throw PlayerControlsVisibilityFingerprint.exception
        } ?: throw YouTubeControlsOverlayFingerprint.exception

        controlsLayoutInflateResult =
            ControlsLayoutInflateFingerprint.result
                ?: throw ControlsLayoutInflateFingerprint.exception

        inflateResult =
            BottomControlsInflateFingerprint.result
                ?: throw BottomControlsInflateFingerprint.exception

        FullscreenEngagementSpeedEduVisibleParentFingerprint.result?.let { parentResult ->
            parentResult.mutableMethod.apply {
                fullscreenEngagementViewVisibleReference =
                    findReference(", isFullscreenEngagementViewVisible=")
                speedEDUVisibleReference = findReference(", isSpeedmasterEDUVisible=")
            }

            fullscreenEngagementSpeedEduVisibleMutableMethod =
                FullscreenEngagementSpeedEduVisibleFingerprint.also {
                    it.resolve(
                        context,
                        parentResult.classDef
                    )
                }.result?.mutableMethod
                    ?: throw FullscreenEngagementSpeedEduVisibleFingerprint.exception
        } ?: throw FullscreenEngagementSpeedEduVisibleParentFingerprint.exception

        ThumbnailPreviewConfigFingerprint.result?.let {
            it.mutableMethod.apply {
                bigBoardsVisibilityMutableMethod = this

                addInstruction(
                    0,
                    "const/4 v0, 0x1"
                )
            }
        } ?: throw ThumbnailPreviewConfigFingerprint.exception
    }

    private lateinit var controlsLayoutInflateResult: MethodFingerprintResult
    private lateinit var inflateResult: MethodFingerprintResult

    private lateinit var bigBoardsVisibilityMutableMethod: MutableMethod
    private lateinit var playerControlsVisibilityMutableMethod: MutableMethod
    private lateinit var quickSeekVisibleMutableMethod: MutableMethod
    private lateinit var seekEDUVisibleMutableMethod: MutableMethod
    private lateinit var userScrubbingMutableMethod: MutableMethod

    private lateinit var fullscreenEngagementSpeedEduVisibleMutableMethod: MutableMethod
    private lateinit var fullscreenEngagementViewVisibleReference: Reference
    private lateinit var speedEDUVisibleReference: Reference

    private fun injectBigBoardsVisibilityCall(descriptor: String) {
        bigBoardsVisibilityMutableMethod.apply {
            addInstruction(
                1,
                "invoke-static {v0}, $descriptor->changeVisibilityNegatedImmediate(Z)V"
            )
        }
    }

    private fun injectFullscreenEngagementSpeedEduViewVisibilityCall(
        reference: Reference,
        descriptor: String
    ) {
        fullscreenEngagementSpeedEduVisibleMutableMethod.apply {
            for ((index, instruction) in implementation!!.instructions.withIndex()) {
                if (instruction.opcode != Opcode.IPUT_BOOLEAN) continue
                if (getInstruction<ReferenceInstruction>(index).reference != reference) continue

                val register = getInstruction<TwoRegisterInstruction>(index).registerA

                addInstruction(
                    index,
                    "invoke-static {v$register}, $descriptor->changeVisibilityNegatedImmediate(Z)V"
                )
                break
            }
        }
    }

    private fun MutableMethod.injectVisibilityCall(
        descriptor: String,
        fieldName: String
    ) {
        addInstruction(
            0,
            "invoke-static {p1}, $descriptor->$fieldName(Z)V"
        )
    }

    private fun MethodFingerprintResult.injectCalls(
        descriptor: String
    ) {
        mutableMethod.apply {
            val endIndex = scanResult.patternScanResult!!.endIndex
            val viewRegister = getInstruction<OneRegisterInstruction>(endIndex).registerA

            addInstruction(
                endIndex + 1,
                "invoke-static {v$viewRegister}, $descriptor->initialize(Ljava/lang/Object;)V"
            )
        }
    }

    internal fun injectVisibility(descriptor: String) {
        playerControlsVisibilityMutableMethod.injectVisibilityCall(
            descriptor,
            "changeVisibility"
        )
        quickSeekVisibleMutableMethod.injectVisibilityCall(
            descriptor,
            "changeVisibilityNegatedImmediate"
        )
        seekEDUVisibleMutableMethod.injectVisibilityCall(
            descriptor,
            "changeVisibilityNegatedImmediate"
        )
        userScrubbingMutableMethod.injectVisibilityCall(
            descriptor,
            "changeVisibilityNegatedImmediate"
        )

        injectBigBoardsVisibilityCall(descriptor)

        injectFullscreenEngagementSpeedEduViewVisibilityCall(
            fullscreenEngagementViewVisibleReference,
            descriptor
        )
        injectFullscreenEngagementSpeedEduViewVisibilityCall(
            speedEDUVisibleReference,
            descriptor
        )
    }

    internal fun initializeSB(descriptor: String) {
        controlsLayoutInflateResult.injectCalls(descriptor)
    }

    internal fun initializeControl(descriptor: String) {
        inflateResult.injectCalls(descriptor)
    }
}