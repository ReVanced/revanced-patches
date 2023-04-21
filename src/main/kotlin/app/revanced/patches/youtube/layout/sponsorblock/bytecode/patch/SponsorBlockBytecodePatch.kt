package app.revanced.patches.youtube.layout.sponsorblock.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.fingerprints.SeekbarFingerprint
import app.revanced.patches.shared.fingerprints.SeekbarOnDrawFingerprint
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility
import app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints.*
import app.revanced.patches.youtube.layout.sponsorblock.resource.patch.SponsorBlockResourcePatch
import app.revanced.patches.youtube.misc.autorepeat.fingerprints.AutoRepeatFingerprint
import app.revanced.patches.youtube.misc.autorepeat.fingerprints.AutoRepeatParentFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playercontrols.bytecode.patch.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.video.information.patch.VideoInformationPatch
import app.revanced.patches.youtube.misc.video.videoid.patch.VideoIdPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.*
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference

@Patch
@DependsOn(
    dependencies = [
        IntegrationsPatch::class,
        VideoIdPatch::class,
        // Required to skip segments on time.
        VideoInformationPatch::class,
        // Used to prevent SponsorBlock from running on Shorts because SponsorBlock does not yet support Shorts.
        PlayerTypeHookPatch::class,
        PlayerControlsBytecodePatch::class,
        SponsorBlockResourcePatch::class,
    ]
)
@Name("sponsorblock")
@Description("Integrates SponsorBlock which allows skipping video segments such as sponsored content.")
@SponsorBlockCompatibility
@Version("0.0.1")
class SponsorBlockBytecodePatch : BytecodePatch(
    listOf(
        SeekbarFingerprint,
        AppendTimeFingerprint,
        PlayerOverlaysLayoutInitFingerprint,
        AutoRepeatParentFingerprint,
    )
) {

    private companion object {
        const val INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/sponsorblock/SegmentPlaybackController;"
        const val INTEGRATIONS_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/sponsorblock/ui/CreateSegmentButtonController;"
        const val INTEGRATIONS_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/sponsorblock/ui/VotingButtonController;"
        const val INTEGRATIONS_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/sponsorblock/ui/SponsorBlockViewController;"
    }

    override fun execute(context: BytecodeContext): PatchResult {
        /*
         * Hook the video time methods
         */
        with(VideoInformationPatch) {
            videoTimeHook(
                INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR,
                "setVideoTime"
            )
        }

        /*
         * Set current video id
         */
        VideoIdPatch.injectCallBackgroundPlay("$INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setCurrentVideoId(Ljava/lang/String;)V")

        /*
         * Seekbar drawing
         */
        val seekbarSignatureResult = SeekbarFingerprint.result!!.let {
            SeekbarOnDrawFingerprint.apply { resolve(context, it.mutableClass) }
        }.result!!
        val seekbarMethod = seekbarSignatureResult.mutableMethod
        val seekbarMethodInstructions = seekbarMethod.implementation!!.instructions

        /*
         * Get the instance of the seekbar rectangle
         */
        for ((index, instruction) in seekbarMethodInstructions.withIndex()) {
            if (instruction.opcode != Opcode.MOVE_OBJECT_FROM16) continue
            seekbarMethod.addInstruction(
                index + 1,
                "invoke-static/range {p0 .. p0}, $INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarRect(Ljava/lang/Object;)V"
            )
            break
        }

        for ((index, instruction) in seekbarMethodInstructions.withIndex()) {
            if (instruction.opcode != Opcode.INVOKE_STATIC) continue

            val invokeInstruction = instruction as Instruction35c
            if ((invokeInstruction.reference as MethodReference).name != "round") continue

            val insertIndex = index + 2

            // set the thickness of the segment
            seekbarMethod.addInstruction(
                insertIndex,
                "invoke-static {v${invokeInstruction.registerC}}, $INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarThickness(I)V"
            )
            break
        }

        /*
         * Set rectangle absolute left and right positions
         */
        val drawRectangleInstructions = seekbarMethodInstructions.withIndex().filter { (_, instruction) ->
            instruction is ReferenceInstruction && (instruction.reference as? MethodReference)?.name == "drawRect"
        }.map { (index, instruction) -> // TODO: improve code
            index to (instruction as FiveRegisterInstruction).registerD
        }

        val (indexRight, rectangleRightRegister) = drawRectangleInstructions[0]
        val (indexLeft, rectangleLeftRegister) = drawRectangleInstructions[3]

        // order of operation is important here due to the code above which has to be improved
        // the reason for that is that we get the index, add instructions and then the offset would be wrong
        seekbarMethod.addInstruction(
            indexLeft + 1,
            "invoke-static {v$rectangleLeftRegister}, $INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarAbsoluteLeft(Landroid/graphics/Rect;)V"
        )
        seekbarMethod.addInstruction(
            indexRight + 1,
            "invoke-static {v$rectangleRightRegister}, $INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarAbsoluteRight(Landroid/graphics/Rect;)V"
        )

        /*
         * Draw segment
         */
        val drawSegmentInstructionInsertIndex = (seekbarMethodInstructions.size - 1 - 2)
        val (canvasInstance, centerY) = (seekbarMethodInstructions[drawSegmentInstructionInsertIndex] as FiveRegisterInstruction).let {
            it.registerC to it.registerE
        }
        seekbarMethod.addInstruction(
            drawSegmentInstructionInsertIndex,
            "invoke-static {v$canvasInstance, v$centerY}, $INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->drawSponsorTimeBars(Landroid/graphics/Canvas;F)V"
        )

        /*
         * Voting & Shield button
         */
        val controlsMethodResult = PlayerControlsBytecodePatch.showPlayerControlsFingerprintResult

        val controlsLayoutStubResourceId =
            ResourceMappingPatch.resourceMappings.single { it.type == "id" && it.name == "controls_layout_stub" }.id
        val zoomOverlayResourceId =
            ResourceMappingPatch.resourceMappings.single { it.type == "id" && it.name == "video_zoom_overlay_stub" }.id

        methods@ for (method in controlsMethodResult.mutableClass.methods) {
            val instructions = method.implementation?.instructions!!
            instructions@ for ((index, instruction) in instructions.withIndex()) {
                // search for method which inflates the controls layout view
                if (instruction.opcode != Opcode.CONST) continue@instructions

                when ((instruction as NarrowLiteralInstruction).wideLiteral) {
                    controlsLayoutStubResourceId -> {
                        // replace the view with the YouTubeControlsOverlay
                        val moveResultInstructionIndex = index + 5
                        val inflatedViewRegister =
                            (instructions[moveResultInstructionIndex] as OneRegisterInstruction).registerA
                        // initialize with the player overlay object
                        method.addInstructions(
                            moveResultInstructionIndex + 1, // insert right after moving the view to the register and use that register
                            """
                                invoke-static {v$inflatedViewRegister}, $INTEGRATIONS_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR->initialize(Ljava/lang/Object;)V
                                invoke-static {v$inflatedViewRegister}, $INTEGRATIONS_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR->initialize(Ljava/lang/Object;)V
                            """
                        )
                    }

                    zoomOverlayResourceId -> {
                        val invertVisibilityMethod =
                            context.toMethodWalker(method).nextMethod(index - 6, true).getMethod() as MutableMethod
                        // change visibility of the buttons
                        invertVisibilityMethod.addInstructions(
                            0, """
                                invoke-static {p1}, $INTEGRATIONS_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR->changeVisibilityNegatedImmediate(Z)V
                                invoke-static {p1}, $INTEGRATIONS_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR->changeVisibilityNegatedImmediate(Z)V
                            """.trimIndent()
                        )
                    }
                }
            }
        }

        // change visibility of the buttons
        PlayerControlsBytecodePatch.injectVisibilityCheckCall("$INTEGRATIONS_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR->changeVisibility(Z)V")
        PlayerControlsBytecodePatch.injectVisibilityCheckCall("$INTEGRATIONS_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR->changeVisibility(Z)V")

        // append the new time to the player layout
        val appendTimeFingerprintResult = AppendTimeFingerprint.result!!
        val appendTimePatternScanStartIndex = appendTimeFingerprintResult.scanResult.patternScanResult!!.startIndex
        val targetRegister =
            (appendTimeFingerprintResult.method.implementation!!.instructions.elementAt(appendTimePatternScanStartIndex + 1) as OneRegisterInstruction).registerA

        appendTimeFingerprintResult.mutableMethod.addInstructions(
            appendTimePatternScanStartIndex + 2, """
                    invoke-static {v$targetRegister}, $INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->appendTimeWithoutSegments(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$targetRegister
            """
        )

        // initialize the player controller
        VideoInformationPatch.onCreateHook(INTEGRATIONS_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR, "initialize")

        // initialize the sponsorblock view
        PlayerOverlaysLayoutInitFingerprint.result!!.mutableMethod.addInstruction(
            6, // after inflating the view
            "invoke-static {p0}, $INTEGRATIONS_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->initialize(Ljava/lang/Object;)V"
        )

        // get rectangle field name
        RectangleFieldInvalidatorFingerprint.resolve(context, seekbarSignatureResult.classDef)
        val rectangleFieldInvalidatorInstructions =
            RectangleFieldInvalidatorFingerprint.result!!.method.implementation!!.instructions
        val rectangleFieldName =
            ((rectangleFieldInvalidatorInstructions.elementAt(rectangleFieldInvalidatorInstructions.count() - 3) as ReferenceInstruction).reference as FieldReference).name

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
                        index, "const-string v$register, \"$with\""
                    )
                }
                for ((index, it) in method.implementation!!.instructions.withIndex()) {
                    if (it.opcode.ordinal != Opcode.CONST_STRING.ordinal) continue

                    when (((it as ReferenceInstruction).reference as StringReference).string) {
                        "replaceMeWithsetSponsorBarRect" -> method.replaceStringInstruction(
                            index,
                            it,
                            rectangleFieldName
                        )
                    }
                }
            } ?: return PatchResultError("Could not find the method which contains the replaceMeWith* strings")


        // The vote and create segment buttons automatically change their visibility when appropriate,
        // but if buttons are showing when the end of the video is reached then they will not automatically hide.
        // Add a hook to forcefully hide when the end of the video is reached.
        AutoRepeatParentFingerprint.result ?: return AutoRepeatParentFingerprint.toErrorResult()
        AutoRepeatFingerprint.also {
            it.resolve(context, AutoRepeatParentFingerprint.result!!.classDef)
        }.result?.mutableMethod?.addInstruction(
            0,
            "invoke-static {}, $INTEGRATIONS_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->endOfVideoReached()V"
        ) ?: return AutoRepeatFingerprint.toErrorResult()

        // TODO: isSBChannelWhitelisting implementation

        return PatchResultSuccess()
    }
}
