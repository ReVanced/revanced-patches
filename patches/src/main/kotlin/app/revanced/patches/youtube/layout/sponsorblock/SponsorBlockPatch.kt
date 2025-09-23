package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playercontrols.*
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
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
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private val sponsorBlockResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
        playerControlsPatch,
    )

    execute {
        addResources("youtube", "layout.sponsorblock.sponsorBlockResourcePatch")

        PreferenceScreen.SPONSORBLOCK.addPreferences(
            // SB setting is old code with lots of custom preferences and updating behavior.
            // Added as a preference group and not a fragment so the preferences are searchable.
            PreferenceCategory(
                key = "revanced_settings_screen_10_sponsorblock",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = emptySet(), // Preferences are added by custom class at runtime.
                tag = "app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup"
            ),
            PreferenceCategory(
                key = "revanced_sb_stats",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = emptySet(), // Preferences are added by custom class at runtime.
                tag = "app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockStatsPreferenceCategory"
            ),
            PreferenceCategory(
                key = "revanced_sb_about",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    NonInteractivePreference(
                        key = "revanced_sb_about_api",
                        tag = "app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockAboutPreference",
                        selectable = true,
                    )
                )
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
            )
        ).forEach { resourceGroup ->
            copyResources("sponsorblock", resourceGroup)
        }

        addTopControl("sponsorblock")
    }
}

private const val EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/sponsorblock/SegmentPlaybackController;"
private const val EXTENSION_CREATE_SEGMENT_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/sponsorblock/ui/CreateSegmentButton;"
private const val EXTENSION_VOTING_BUTTON_CONTROLLER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/sponsorblock/ui/VotingButton;"
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
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
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
        val appendTimePatternScanStartIndex = appendTimeFingerprint.patternMatch!!.startIndex
        appendTimeFingerprint.method.apply {
            val register = getInstruction<OneRegisterInstruction>(appendTimePatternScanStartIndex + 1).registerA

            addInstructions(
                appendTimePatternScanStartIndex + 2,
                """
                    invoke-static { v$register }, $EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->appendTimeWithoutSegments(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$register
                """
            )
        }

        // Initialize the player controller.
        onCreateHook(EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR, "initialize")

        // Initialize the SponsorBlock view.
        controlsOverlayFingerprint.match(layoutConstructorFingerprint.originalClassDef).let {
            val startIndex = it.patternMatch!!.startIndex
            it.method.apply {
                val frameLayoutRegister = (getInstruction(startIndex + 2) as OneRegisterInstruction).registerA
                addInstruction(
                    startIndex + 3,
                    "invoke-static {v$frameLayoutRegister}, $EXTENSION_SPONSORBLOCK_VIEW_CONTROLLER_CLASS_DESCRIPTOR->initialize(Landroid/view/ViewGroup;)V",
                )
            }
        }

        // Set seekbar draw rectangle.
        rectangleFieldInvalidatorFingerprint.match(seekbarOnDrawFingerprint.originalClassDef).method.apply {
            val fieldIndex = instructions.count() - 3
            val fieldReference = getInstruction<ReferenceInstruction>(fieldIndex).reference as FieldReference

            // replace the "replaceMeWith*" strings
            proxy(classes.first { it.type.endsWith("SegmentPlaybackController;") })
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
                    for ((index, it) in method.instructions.withIndex()) {
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

        adProgressTextViewVisibilityFingerprint.method.apply {
            val index = indexOfAdProgressTextViewVisibilityInstruction(this)
            val register = getInstruction<FiveRegisterInstruction>(index).registerD

            addInstructionsAtControlFlowLabel(
                index,
                "invoke-static { v$register }, $EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR->setAdProgressTextVisibility(I)V"
            )
        }

    }
}
