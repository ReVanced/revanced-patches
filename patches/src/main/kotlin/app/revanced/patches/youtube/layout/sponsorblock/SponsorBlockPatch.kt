package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playercontrols.*
import app.revanced.patches.youtube.misc.playercontrols.addTopControl
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.addSettingPreference
import app.revanced.patches.youtube.misc.settings.newIntent
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.*
import app.revanced.patches.youtube.video.information.onCreateHook
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.patches.youtube.video.information.videoTimeHook
import app.revanced.patches.youtube.video.videoid.hookBackgroundPlayVideoId
import app.revanced.patches.youtube.video.videoid.videoIdPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.*
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private val sponsorBlockResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
        playerControlsPatch
    )

    execute { context ->
        addResources("youtube", "layout.sponsorblock.sponsorBlockResourcePatch")

        addSettingPreference(
            IntentPreference(
                key = "revanced_settings_screen_10",
                titleKey = "revanced_sb_settings_title",
                summaryKey = null,
                intent = newIntent("revanced_sb_settings_intent"),
            )
        )

        arrayOf(
            ResourceGroup(
                "layout",
                "revanced_sb_inline_sponsor_overlay.xml",
                "revanced_sb_new_segment.xml",
                "revanced_sb_skip_sponsor_button.xml",
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable",
                "revanced_sb_adjust.xml",
                "revanced_sb_backward.xml",
                "revanced_sb_compare.xml",
                "revanced_sb_edit.xml",
                "revanced_sb_forward.xml",
                "revanced_sb_logo.xml",
                "revanced_sb_publish.xml",
                "revanced_sb_voting.xml",
            ),
            ResourceGroup(
                // required resource for back button, because when the base APK is used, this resource will not exist
                "drawable-xxxhdpi",
                "quantum_ic_skip_next_white_24.png",
            ),
        ).forEach { resourceGroup ->
            context.copyResources("sponsorblock", resourceGroup)
        }

        addTopControl("sponsorblock")
    }
}

private const val EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/sponsorblock/SegmentPlaybackController;"
private const val EXTENSION_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/sponsorblock/ui/CreateSegmentButtonController;"
private const val EXTENSION_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/sponsorblock/ui/VotingButtonController;"
private const val EXTENSION_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/sponsorblock/ui/SponsorBlockViewController;"

@Suppress("unused")
val sponsorBlockPatch = bytecodePatch(
    name = "SponsorBlock",
    description = "Adds options to enable and configure SponsorBlock, which can skip undesired video segments such as sponsored content.",
) {
    dependsOn(
        sharedExtensionPatch,
        videoIdPatch,
        // Required to skip segments on time.
        videoInformationPatch,
        // Used to prevent SponsorBlock from running on Shorts because SponsorBlock does not yet support Shorts.
        playerTypeHookPatch,
        playerControlsPatch,
        sponsorBlockResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val seekbarMatch by seekbarFingerprint()
    val appendTimeMatch by appendTimeFingerprint()
    val layoutConstructorMatch by layoutConstructorFingerprint()
    val autoRepeatParentMatch by autoRepeatParentFingerprint()

    execute { context ->
        /*
         * Hook the video time methods
         */
        videoTimeHook(
            EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR,
            "setVideoTime",
        )

        /*
         * Set current video id.
         */
        hookBackgroundPlayVideoId(
            "$EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->" +
                    "setCurrentVideoId(Ljava/lang/String;)V",
        )

        /*
         * Seekbar drawing
         */
        val seekbarSignatureMatch =
            seekbarOnDrawFingerprint.apply { match(context, seekbarMatch.mutableClass) }.matchOrThrow()
        val seekbarMethod = seekbarSignatureMatch.mutableMethod
        val seekbarMethodInstructions = seekbarMethod.implementation!!.instructions

        /*
         * Get left and right of seekbar rectangle
         */
        val moveRectangleToRegisterIndex = seekbarMethodInstructions.indexOfFirst {
            it.opcode == Opcode.MOVE_OBJECT_FROM16
        }

        seekbarMethod.addInstruction(
            moveRectangleToRegisterIndex + 1,
            "invoke-static/range {p0 .. p0}, " +
                    "$EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarRect(Ljava/lang/Object;)V",
        )

        for ((index, instruction) in seekbarMethodInstructions.withIndex()) {
            if (instruction.opcode != Opcode.INVOKE_STATIC) continue

            val invokeInstruction = instruction as Instruction35c
            if ((invokeInstruction.reference as MethodReference).name != "round") continue

            val insertIndex = index + 2

            // set the thickness of the segment
            seekbarMethod.addInstruction(
                insertIndex,
                "invoke-static {v${invokeInstruction.registerC}}, " +
                        "$EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarThickness(I)V",
            )

            break
        }

        /*
         * Draw segment
         */
        // Find the drawCircle call and draw the segment before it
        for (i in seekbarMethodInstructions.size - 1 downTo 0) {
            val invokeInstruction = seekbarMethodInstructions[i] as? ReferenceInstruction ?: continue
            if ((invokeInstruction.reference as MethodReference).name != "drawCircle") continue

            val (canvasInstance, centerY) = (invokeInstruction as FiveRegisterInstruction).let {
                it.registerC to it.registerE
            }
            seekbarMethod.addInstruction(
                i,
                "invoke-static {v$canvasInstance, v$centerY}, " +
                        "$EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->" +
                        "drawSponsorTimeBars(Landroid/graphics/Canvas;F)V",
            )

            break
        }

        // Change visibility of the buttons.
        initializeTopControl(EXTENSION_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)
        injectVisibilityCheckCall(EXTENSION_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)

        initializeTopControl(EXTENSION_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)
        injectVisibilityCheckCall(EXTENSION_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)


        // Append the new time to the player layout.
        val appendTimePatternScanStartIndex = appendTimeMatch.patternMatch!!.startIndex
        val targetRegister =
            (appendTimeMatch.method.implementation!!.instructions.elementAt(appendTimePatternScanStartIndex + 1) as OneRegisterInstruction).registerA

        appendTimeMatch.mutableMethod.addInstructions(
            appendTimePatternScanStartIndex + 2,
            """
                invoke-static {v$targetRegister}, $EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->appendTimeWithoutSegments(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$targetRegister
            """,
        )

        // initialize the player controller
        onCreateHook(EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR, "initialize")

        // initialize the sponsorblock view
        controlsOverlayFingerprint.apply {
            match(context, layoutConstructorMatch.classDef)
        }.matchOrThrow().let {
            val startIndex = it.patternMatch!!.startIndex
            it.mutableMethod.apply {
                val frameLayoutRegister = (getInstruction(startIndex + 2) as OneRegisterInstruction).registerA
                addInstruction(
                    startIndex + 3,
                    "invoke-static {v$frameLayoutRegister}, $EXTENSION_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->initialize(Landroid/view/ViewGroup;)V",
                )
            }
        }

        // get rectangle field name
        rectangleFieldInvalidatorFingerprint.match(context, seekbarSignatureMatch.classDef)
        val rectangleFieldInvalidatorInstructions =
            rectangleFieldInvalidatorFingerprint.match!!.method.implementation!!.instructions
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
                            rectangleFieldName,
                        )
                    }
                }
            } ?: throw PatchException("Could not find the method which contains the replaceMeWith* strings")

        // The vote and create segment buttons automatically change their visibility when appropriate,
        // but if buttons are showing when the end of the video is reached then they will not automatically hide.
        // Add a hook to forcefully hide when the end of the video is reached.
        autoRepeatFingerprint.also {
            it.match(context, autoRepeatParentMatch.classDef)
        }.matchOrThrow().mutableMethod.addInstruction(
            0,
            "invoke-static {}, $EXTENSION_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->endOfVideoReached()V",
        )

        // TODO: isSBChannelWhitelisting implementation
    }
}
