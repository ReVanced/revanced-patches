package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playercontrols.*
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
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private val sponsorBlockResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
        playerControlsPatch,
    )

    execute {
        addResources("youtube", "layout.sponsorblock.sponsorBlockResourcePatch")

        addSettingPreference(
            IntentPreference(
                key = "revanced_settings_screen_10",
                titleKey = "revanced_sb_settings_title",
                summaryKey = null,
                intent = newIntent("revanced_sb_settings_intent"),
            ),
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
            copyResources("sponsorblock", resourceGroup)
        }

        addTopControl("sponsorblock")
    }
}

internal const val EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR =
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
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        // Hook the video time methods.
        videoTimeHook(
            EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR,
            "setVideoTime",
        )

        hookBackgroundPlayVideoId(
            EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR +
                "->setCurrentVideoId(Ljava/lang/String;)V",
        )

        // Seekbar drawing
        seekbarOnDrawFingerprint.match(seekbarFingerprint.originalClassDef).method.apply {
            // Get left and right of seekbar rectangle.
            val moveRectangleToRegisterIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_OBJECT_FROM16)

            addInstruction(
                moveRectangleToRegisterIndex + 1,
                "invoke-static/range { p0 .. p0 }, " +
                    "$EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarRect(Ljava/lang/Object;)V",
            )

            // Set the thickness of the segment.
            val thicknessIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_STATIC && getReference<MethodReference>()?.name == "round"
            }
            val thicknessRegister = getInstruction<FiveRegisterInstruction>(thicknessIndex).registerC
            addInstruction(
                thicknessIndex + 2,
                "invoke-static { v$thicknessRegister }, " +
                    "$EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setSponsorBarThickness(I)V",
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
                    "$EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->" +
                    "drawSponsorTimeBars(Landroid/graphics/Canvas;F)V",
            )
        }

        // Change visibility of the buttons.
        initializeTopControl(EXTENSION_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)
        injectVisibilityCheckCall(EXTENSION_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)

        initializeTopControl(EXTENSION_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)
        injectVisibilityCheckCall(EXTENSION_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR)

        // Append the new time to the player layout.
        val appendTimePatternScanStartIndex = appendTimeFingerprint.filterMatches.first().index
        appendTimeFingerprint.method.apply {
            val register = getInstruction<OneRegisterInstruction>(appendTimePatternScanStartIndex + 1).registerA

            addInstructions(
                appendTimePatternScanStartIndex + 2,
                """
                    invoke-static { v$register }, $EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->appendTimeWithoutSegments(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$register
                """,
            )
        }

        // Initialize the player controller.
        onCreateHook(EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR, "initialize")

        // Initialize the SponsorBlock view.
        controlsOverlayFingerprint.match(layoutConstructorFingerprint.originalClassDef).let {
            val startIndex = it.filterMatches.first().index
            it.method.apply {
                val frameLayoutRegister = (getInstruction(startIndex + 2) as OneRegisterInstruction).registerA
                addInstruction(
                    startIndex + 3,
                    "invoke-static {v$frameLayoutRegister}, $EXTENSION_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->initialize(Landroid/view/ViewGroup;)V",
                )
            }
        }

        // Set seekbar draw rectangle.
        rectangleFieldInvalidatorFingerprint.match(seekbarFingerprint.originalClassDef).method.apply {
            val invalidateIndex = indexOfInvalidateInstruction(this)
            val rectangleIndex = indexOfFirstInstructionReversedOrThrow(invalidateIndex + 1) {
                getReference<FieldReference>()?.type == "Landroid/graphics/Rect;"
            }
            val rectangleFieldName =
                (getInstruction<ReferenceInstruction>(rectangleIndex).reference as FieldReference).name

            segmentPlaybackControllerFingerprint.let {
                it.method.apply {
                    val replaceIndex = it.patternMatch.startIndex
                    val replaceRegister =
                        getInstruction<OneRegisterInstruction>(replaceIndex).registerA

                    replaceInstruction(
                        replaceIndex,
                        "const-string v$replaceRegister, \"$rectangleFieldName\""
                    )
                }
            }
        }

        // The vote and create segment buttons automatically change their visibility when appropriate,
        // but if buttons are showing when the end of the video is reached then they will not automatically hide.
        // Add a hook to forcefully hide when the end of the video is reached.
        autoRepeatFingerprint.match(autoRepeatParentFingerprint.originalClassDef).method.addInstruction(
            0,
            "invoke-static {}, $EXTENSION_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->endOfVideoReached()V",
        )

        // TODO: Channel whitelisting.
    }
}
