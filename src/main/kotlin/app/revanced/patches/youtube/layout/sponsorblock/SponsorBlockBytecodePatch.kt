package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.sponsorblock.fingerprints.AppendTimeFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.fingerprints.ControlsOverlayFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.fingerprints.RectangleFieldInvalidatorFingerprint
import app.revanced.patches.youtube.misc.autorepeat.fingerprints.AutoRepeatFingerprint
import app.revanced.patches.youtube.misc.autorepeat.fingerprints.AutoRepeatParentFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.shared.fingerprints.LayoutConstructorFingerprint
import app.revanced.patches.youtube.shared.fingerprints.SeekbarFingerprint
import app.revanced.patches.youtube.shared.fingerprints.SeekbarOnDrawFingerprint
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import app.revanced.patches.youtube.video.videoid.VideoIdPatch
import app.revanced.util.alsoResolve
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "SponsorBlock",
    description = "Adds options to enable and configure SponsorBlock, which can skip undesired video segments such as sponsored content.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
    dependencies = [
        IntegrationsPatch::class,
        VideoIdPatch::class,
        // Required to skip segments on time.
        VideoInformationPatch::class,
        // Used to prevent SponsorBlock from running on Shorts because SponsorBlock does not yet support Shorts.
        PlayerTypeHookPatch::class,
        PlayerControlsBytecodePatch::class,
        SponsorBlockResourcePatch::class,
    ],
)
@Suppress("unused")
object SponsorBlockBytecodePatch : BytecodePatch(
    setOf(
        SeekbarFingerprint,
        AppendTimeFingerprint,
        LayoutConstructorFingerprint,
        AutoRepeatParentFingerprint,
    ),
) {
    private const val INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/sponsorblock/SegmentPlaybackController;"
    private const val INTEGRATIONS_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/sponsorblock/ui/CreateSegmentButtonController;"
    private const val INTEGRATIONS_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/sponsorblock/ui/VotingButtonController;"
    private const val INTEGRATIONS_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/sponsorblock/ui/SponsorBlockViewController;"

    override fun execute(context: BytecodeContext) {
        /*
         * Hook the video time methods
         */
        with(VideoInformationPatch) {
            videoTimeHook(
                INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR,
                "setVideoTime",
            )
        }

        VideoIdPatch.hookBackgroundPlayVideoId(
            "$INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setCurrentVideoId(Ljava/lang/String;)V")


        // Seekbar drawing
        SeekbarOnDrawFingerprint.alsoResolve(context, SeekbarFingerprint).mutableMethod.apply {
            // Get left and right of seekbar rectangle.
            val moveRectangleToRegisterIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_OBJECT_FROM16)

            addInstruction(
                moveRectangleToRegisterIndex + 1,
                "invoke-static/range { p0 .. p0 }, " +
                        "$INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarRect(Ljava/lang/Object;)V",
            )

            // Set the thickness of the segment.
            val thicknessIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_STATIC && getReference<MethodReference>()?.name == "round"
            }
            val thicknessRegister = getInstruction<FiveRegisterInstruction>(thicknessIndex).registerC
            addInstruction(
                thicknessIndex + 2,
                "invoke-static { v$thicknessRegister }, " +
                        "$INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarThickness(I)V",
            )

            // Find the drawCircle call and draw the segment before it.
            val drawCircleIndex = indexOfFirstInstructionReversedOrThrow {
                getReference<MethodReference>()?.name == "drawCircle"
            }
            val drawCircleInstruction = getInstruction<FiveRegisterInstruction>(drawCircleIndex)
            val canvasInstanceRegister = drawCircleInstruction.registerC
            val centerYRegister = drawCircleInstruction.registerE

            addInstruction(
                drawCircleIndex,
                "invoke-static { v$canvasInstanceRegister, v$centerYRegister }, " +
                        "$INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->" +
                        "drawSponsorTimeBars(Landroid/graphics/Canvas;F)V",
            )
        }

        // Change visibility of the buttons.
        PlayerControlsBytecodePatch.initializeTopControl(INTEGRATIONS_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)
        PlayerControlsBytecodePatch.injectVisibilityCheckCall(INTEGRATIONS_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)

        PlayerControlsBytecodePatch.initializeTopControl(INTEGRATIONS_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)
        PlayerControlsBytecodePatch.injectVisibilityCheckCall(INTEGRATIONS_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)

        // Append the new time to the player layout.
        AppendTimeFingerprint.resultOrThrow().let {
            val appendTimePatternScanStartIndex = it.scanResult.patternScanResult!!.startIndex
            it.mutableMethod.apply {
                val register = getInstruction<OneRegisterInstruction>(appendTimePatternScanStartIndex + 1).registerA

                addInstructions(
                    appendTimePatternScanStartIndex + 2,
                    """
                        invoke-static { v$register }, $INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->appendTimeWithoutSegments(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """
                )
            }
        }

        // Initialize the player controller.
        VideoInformationPatch.onCreateHook(INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR, "initialize")

        // Initialize the SponsorBlock view.
        ControlsOverlayFingerprint.alsoResolve(context, LayoutConstructorFingerprint).let {
            val startIndex = it.scanResult.patternScanResult!!.startIndex
            it.mutableMethod.apply {
                val frameLayoutRegister = (getInstruction(startIndex + 2) as OneRegisterInstruction).registerA
                addInstruction(
                    startIndex + 3,
                    "invoke-static {v$frameLayoutRegister}, $INTEGRATIONS_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->initialize(Landroid/view/ViewGroup;)V",
                )
            }
        }


        // Set seekbar draw rectangle.
        RectangleFieldInvalidatorFingerprint.alsoResolve(context, SeekbarOnDrawFingerprint).mutableMethod.apply {
            val fieldIndex = implementation!!.instructions.count() - 3
            val fieldReference = getInstruction<ReferenceInstruction>(fieldIndex).reference as FieldReference

            // replace the "replaceMeWith*" strings
            context
                .proxy(context.classes.first { it.type.endsWith("SegmentPlaybackController;") })
                .mutableClass
                .methods
                .find { it.name == "setSponsorBarRect" }
                ?.let { method ->
                    fun MutableMethod.replaceStringInstruction(index: Int, instruction: Instruction, with: String) {
                        val register = (instruction as OneRegisterInstruction).registerA
                        this.replaceInstruction(
                            index,
                            "const-string v$register, \"$with\"",
                        )
                    }
                    for ((index, it) in method.implementation!!.instructions.withIndex()) {
                        if (it.opcode.ordinal != Opcode.CONST_STRING.ordinal) continue

                        when (((it as ReferenceInstruction).reference as StringReference).string) {
                            "replaceMeWithsetSponsorBarRect" -> method.replaceStringInstruction(
                                index,
                                it,
                                fieldReference.name,
                            )
                        }
                    }
                } ?: throw PatchException("Could not find the method which contains the replaceMeWith* strings")
        }

        // The vote and create segment buttons automatically change their visibility when appropriate,
        // but if buttons are showing when the end of the video is reached then they will not automatically hide.
        // Add a hook to forcefully hide when the end of the video is reached.
        AutoRepeatFingerprint.alsoResolve(context, AutoRepeatParentFingerprint).mutableMethod.addInstruction(
            0,
            "invoke-static {}, $INTEGRATIONS_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->endOfVideoReached()V",
        )

        // TODO: isSBChannelWhitelisting implementation?
    }
}
