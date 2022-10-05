package app.revanced.patches.youtube.layout.sponsorblock.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.layout.autocaptions.fingerprints.StartVideoInformerFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility
import app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints.*
import app.revanced.patches.youtube.layout.sponsorblock.resource.patch.SponsorBlockResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.playercontrols.bytecode.patch.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.misc.videoid.patch.VideoIdPatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.iface.instruction.*
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodParameter
import org.jf.dexlib2.util.MethodUtil

@Patch
@DependsOn(
    dependencies = [PlayerControlsBytecodePatch::class, IntegrationsPatch::class, SponsorBlockResourcePatch::class, VideoIdPatch::class]
)
@Name("sponsorblock")
@Description("Integrate SponsorBlock.")
@SponsorBlockCompatibility
@Version("0.0.1")
class SponsorBlockBytecodePatch : BytecodePatch(
    listOf(
        PlayerControllerSetTimeReferenceFingerprint,
        CreateVideoPlayerSeekbarFingerprint,
        VideoTimeFingerprint,
        NextGenWatchLayoutFingerprint,
        AppendTimeFingerprint,
        PlayerInitFingerprint,
        PlayerOverlaysLayoutInitFingerprint,
        ShortsPlayerConstructorFingerprint,
        StartVideoInformerFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {/*
        Set current video time
        */
        val referenceResult = PlayerControllerSetTimeReferenceFingerprint.result!!
        val playerControllerSetTimeMethod =
            context.toMethodWalker(referenceResult.method)
                .nextMethod(referenceResult.scanResult.patternScanResult!!.startIndex, true)
                .getMethod() as MutableMethod
        playerControllerSetTimeMethod.addInstruction(
            2,
            "invoke-static {p1, p2}, Lapp/revanced/integrations/sponsorblock/PlayerController;->setCurrentVideoTime(J)V"
        )

        /*
        Set current video time high precision
         */
        val constructorFingerprint =
            object : MethodFingerprint("V", null, listOf("J", "J", "J", "J", "I", "L"), null) {}
        constructorFingerprint.resolve(context, VideoTimeFingerprint.result!!.classDef)

        val constructor = constructorFingerprint.result!!.mutableMethod
        constructor.addInstruction(
            0,
            "invoke-static {p1, p2}, Lapp/revanced/integrations/sponsorblock/PlayerController;->setCurrentVideoTimeHighPrecision(J)V"
        )

        /*
         Set current video id
         */
        VideoIdPatch.injectCall("Lapp/revanced/integrations/sponsorblock/PlayerController;->setCurrentVideoId(Ljava/lang/String;)V")

        /*
         Seekbar drawing
         */
        val seekbarSignatureResult = CreateVideoPlayerSeekbarFingerprint.result!!
        val seekbarMethod = seekbarSignatureResult.mutableMethod
        val seekbarMethodInstructions = seekbarMethod.implementation!!.instructions

        /*
         Get the instance of the seekbar rectangle
         */
        for ((index, instruction) in seekbarMethodInstructions.withIndex()) {
            if (instruction.opcode != Opcode.MOVE_OBJECT_FROM16) continue
            seekbarMethod.addInstruction(
                index + 1,
                "invoke-static {v0}, Lapp/revanced/integrations/sponsorblock/PlayerController;->setSponsorBarRect(Ljava/lang/Object;)V"
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
                "invoke-static {v${invokeInstruction.registerC}}, Lapp/revanced/integrations/sponsorblock/PlayerController;->setSponsorBarThickness(I)V"
            )
            break
        }

        /*
        Set rectangle absolute left and right positions
        */
        val drawRectangleInstructions = seekbarMethodInstructions.filter {
            it is ReferenceInstruction && (it.reference as? MethodReference)?.name == "drawRect" && it is FiveRegisterInstruction
        }.map { // TODO: improve code
            seekbarMethodInstructions.indexOf(it) to (it as FiveRegisterInstruction).registerD
        }

        val (indexRight, rectangleRightRegister) = drawRectangleInstructions[0]
        val (indexLeft, rectangleLeftRegister) = drawRectangleInstructions[3]

        // order of operation is important here due to the code above which has to be improved
        // the reason for that is that we get the index, add instructions and then the offset would be wrong
        seekbarMethod.addInstruction(
            indexLeft + 1,
            "invoke-static {v$rectangleLeftRegister}, Lapp/revanced/integrations/sponsorblock/PlayerController;->setSponsorBarAbsoluteLeft(Landroid/graphics/Rect;)V"
        )
        seekbarMethod.addInstruction(
            indexRight + 1,
            "invoke-static {v$rectangleRightRegister}, Lapp/revanced/integrations/sponsorblock/PlayerController;->setSponsorBarAbsoluteRight(Landroid/graphics/Rect;)V"
        )

        /*
        Draw segment
        */
        val drawSegmentInstructionInsertIndex = (seekbarMethodInstructions.size - 1 - 2)
        val (canvasInstance, centerY) = (seekbarMethodInstructions[drawSegmentInstructionInsertIndex] as FiveRegisterInstruction).let {
            it.registerC to it.registerE
        }
        seekbarMethod.addInstruction(
            drawSegmentInstructionInsertIndex,
            "invoke-static {v$canvasInstance, v$centerY}, Lapp/revanced/integrations/sponsorblock/PlayerController;->drawSponsorTimeBars(Landroid/graphics/Canvas;F)V"
        )

        /*
        Set video length
         */
        VideoLengthFingerprint.resolve(context, seekbarSignatureResult.classDef)
        val videoLengthMethodResult = VideoLengthFingerprint.result!!
        val videoLengthMethod = videoLengthMethodResult.mutableMethod
        val videoLengthMethodInstructions = videoLengthMethod.implementation!!.instructions

        val videoLengthRegister =
            (videoLengthMethodInstructions[videoLengthMethodResult.scanResult.patternScanResult!!.endIndex - 2] as OneRegisterInstruction).registerA
        val dummyRegisterForLong =
            videoLengthRegister + 1 // this is required for long values since they are 64 bit wide
        videoLengthMethod.addInstruction(
            videoLengthMethodResult.scanResult.patternScanResult!!.endIndex,
            "invoke-static {v$videoLengthRegister, v$dummyRegisterForLong}, Lapp/revanced/integrations/sponsorblock/PlayerController;->setVideoLength(J)V"
        )

        /*
        Voting & Shield button
         */
        val controlsMethodResult = PlayerControlsBytecodePatch.showPlayerControlsFingerprintResult

        val controlsLayoutStubResourceId =
            ResourceMappingResourcePatch.resourceMappings.single { it.type == "id" && it.name == "controls_layout_stub" }.id
        val zoomOverlayResourceId =
            ResourceMappingResourcePatch.resourceMappings.single { it.type == "id" && it.name == "video_zoom_overlay_stub" }.id

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
                                invoke-static {v$inflatedViewRegister}, Lapp/revanced/integrations/sponsorblock/ShieldButton;->initialize(Ljava/lang/Object;)V
                                invoke-static {v$inflatedViewRegister}, Lapp/revanced/integrations/sponsorblock/VotingButton;->initialize(Ljava/lang/Object;)V
                            """
                        )
                    }

                    zoomOverlayResourceId -> {
                        val invertVisibilityMethod =
                            context.toMethodWalker(method).nextMethod(index - 6, true).getMethod() as MutableMethod
                        // change visibility of the buttons
                        invertVisibilityMethod.addInstructions(
                            0, """
                                invoke-static {p1}, Lapp/revanced/integrations/sponsorblock/ShieldButton;->changeVisibilityNegatedImmediate(Z)V
                                invoke-static {p1}, Lapp/revanced/integrations/sponsorblock/VotingButton;->changeVisibilityNegatedImmediate(Z)V
                            """.trimIndent()
                        )
                    }
                }
            }
        }

        // change visibility of the buttons
        PlayerControlsBytecodePatch.injectVisibilityCheckCall("Lapp/revanced/integrations/sponsorblock/ShieldButton;->changeVisibility(Z)V")
        PlayerControlsBytecodePatch.injectVisibilityCheckCall("Lapp/revanced/integrations/sponsorblock/VotingButton;->changeVisibility(Z)V")

        // set SegmentHelperLayout.context to the player layout instance
        val instanceRegister = 0
        NextGenWatchLayoutFingerprint.result!!.mutableMethod.addInstruction(
            3, // after super call
            "invoke-static/range {p$instanceRegister}, Lapp/revanced/integrations/sponsorblock/PlayerController;->addSkipSponsorView15(Landroid/view/View;)V"
        )

        // append the new time to the player layout
        val appendTimeFingerprintResult = AppendTimeFingerprint.result!!
        val appendTimePatternScanStartIndex = appendTimeFingerprintResult.scanResult.patternScanResult!!.startIndex
        val targetRegister =
            (appendTimeFingerprintResult.method.implementation!!.instructions.elementAt(appendTimePatternScanStartIndex + 1) as OneRegisterInstruction).registerA

        appendTimeFingerprintResult.mutableMethod.addInstructions(
            appendTimePatternScanStartIndex + 2, """
                    invoke-static {v$targetRegister}, Lapp/revanced/integrations/sponsorblock/SponsorBlockUtils;->appendTimeWithoutSegments(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$targetRegister
            """
        )

        // initialize the player controller
        val initFingerprintResult = PlayerInitFingerprint.result!!
        val initInstanceRegister = 0
        initFingerprintResult.mutableClass.methods.first { MethodUtil.isConstructor(it) }.addInstruction(
            4, // after super class invoke
            "invoke-static {v$initInstanceRegister}, Lapp/revanced/integrations/sponsorblock/PlayerController;->onCreate(Ljava/lang/Object;)V"
        )

        // initialize the sponsorblock view
        PlayerOverlaysLayoutInitFingerprint.result!!.mutableMethod.addInstruction(
            6, // after inflating the view
            "invoke-static {p0}, Lapp/revanced/integrations/sponsorblock/player/ui/SponsorBlockView;->initialize(Ljava/lang/Object;)V"
        )

        // lastly create hooks for the player controller

        // get original seek method
        SeekFingerprint.resolve(context, initFingerprintResult.classDef)
        val seekFingerprintResultMethod = SeekFingerprint.result!!.method
        // get enum type for the seek helper method
        val seekSourceEnumType = seekFingerprintResultMethod.parameterTypes[1].toString()

        // create helper method
        val seekHelperMethod = ImmutableMethod(
            seekFingerprintResultMethod.definingClass,
            "seekHelper",
            listOf(ImmutableMethodParameter("J", null, "time")),
            "Z",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            null, null,
            MutableMethodImplementation(4)
        ).toMutable()

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

        // add the helper method to the original class
        initFingerprintResult.mutableClass.methods.add(seekHelperMethod)

        // get rectangle field name
        RectangleFieldInvalidatorFingerprint.resolve(context, seekbarSignatureResult.classDef)
        val rectangleFieldInvalidatorInstructions =
            RectangleFieldInvalidatorFingerprint.result!!.method.implementation!!.instructions
        val rectangleFieldName =
            ((rectangleFieldInvalidatorInstructions.elementAt(rectangleFieldInvalidatorInstructions.count() - 3) as ReferenceInstruction).reference as FieldReference).name

        // get the player controller class from the integrations
        val playerControllerMethods =
            context.proxy(context.classes.first { it.type.endsWith("PlayerController;") }).mutableClass.methods

        // get the method which contain the "replaceMe" strings
        val replaceMeMethods =
            playerControllerMethods.filter { it.name == "onCreate" || it.name == "setSponsorBarRect" }

        fun MutableMethod.replaceStringInstruction(index: Int, instruction: Instruction, with: String) {
            val register = (instruction as OneRegisterInstruction).registerA
            this.replaceInstruction(
                index, "const-string v$register, \"$with\""
            )
        }

        // replace the "replaceMeWith*" strings
        for (method in replaceMeMethods) {
            for ((index, it) in method.implementation!!.instructions.withIndex()) {
                if (it.opcode.ordinal != Opcode.CONST_STRING.ordinal) continue

                when (((it as ReferenceInstruction).reference as StringReference).string) {
                    "replaceMeWithsetSponsorBarRect" ->
                        method.replaceStringInstruction(index, it, rectangleFieldName)

                    "replaceMeWithsetMillisecondMethod" ->
                        method.replaceStringInstruction(index, it, "seekHelper")
                }
            }
        }

        val startVideoInformerMethod = StartVideoInformerFingerprint.result!!.mutableMethod
        startVideoInformerMethod.addInstructions(
            0, """
            const/4 v0, 0x0
            sput-boolean v0, Lapp/revanced/integrations/sponsorblock/PlayerController;->shorts_playing:Z
        """
        )

        val shortsPlayerConstructorMethod = ShortsPlayerConstructorFingerprint.result!!.mutableMethod

        shortsPlayerConstructorMethod.addInstructions(
            0, """
            const/4 v0, 0x1
            sput-boolean v0, Lapp/revanced/integrations/sponsorblock/PlayerController;->shorts_playing:Z
        """
        )

        // TODO: isSBChannelWhitelisting implementation

        return PatchResultSuccess()
    }
}
