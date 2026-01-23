package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_28_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.getLayoutConstructorMethodMatch
import app.revanced.patches.youtube.shared.subtitleButtonControllerFingerprint
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HidePlayerOverlayButtonsPatch;"

@Suppress("ObjectPropertyName")
val `Hide player overlay buttons` by creatingBytecodePatch(
    description = "Adds options to hide the player Cast, Autoplay, Captions, Previous & Next buttons, and the player " +
        "control buttons background.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        resourceMappingPatch, // Used by fingerprints.
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        ),
    )

    apply {
        addResources("youtube", "layout.buttons.overlay.hidePlayerOverlayButtonsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_player_previous_next_buttons"),
            SwitchPreference("revanced_hide_cast_button"),
            SwitchPreference("revanced_hide_captions_button"),
            SwitchPreference("revanced_hide_autoplay_button"),
            SwitchPreference("revanced_hide_player_control_buttons_background"),
        )

        // region Hide player next/previous button.

        getLayoutConstructorMethodMatch().let {
            val insertIndex = it.indices.last()
            val viewRegister = it.method.getInstruction<FiveRegisterInstruction>(insertIndex).registerC

            it.method.addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, $EXTENSION_CLASS_DESCRIPTOR" +
                    "->hidePreviousNextButtons(Landroid/view/View;)V",
            )
        }

        // endregion

        // region Hide cast button.

        mediaRouteButtonMethod.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->getCastButtonOverrideV2(I)I
                move-result p1
            """,
        )

        if (is_20_28_or_greater) {
            arrayOf(
                castButtonPlayerFeatureFlagMethodMatch,
                castButtonActionFeatureFlagMethodMatch,
            ).forEach { match ->
                match.method.insertLiteralOverride(
                    match.indices.first(),
                    "$EXTENSION_CLASS_DESCRIPTOR->getCastButtonOverrideV2(Z)Z",
                )
            }
        }

        // endregion

        // region Hide captions button.

        subtitleButtonControllerFingerprint.method.apply {
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_BOOLEAN) + 1

            addInstruction(
                insertIndex,
                "invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->hideCaptionsButton(Landroid/widget/ImageView;)V",
            )
        }

        // endregion

        // region Hide autoplay button.

        getLayoutConstructorMethodMatch().method.apply {
            val constIndex = indexOfFirstResourceIdOrThrow("autonav_toggle")
            val constRegister = getInstruction<OneRegisterInstruction>(constIndex).registerA

            // Add a conditional branch around the code that inflates and adds the auto-repeat button.
            val gotoIndex = indexOfFirstInstructionOrThrow(constIndex) {
                val parameterTypes = getReference<MethodReference>()?.parameterTypes
                opcode == Opcode.INVOKE_VIRTUAL &&
                    parameterTypes?.size == 2 &&
                    parameterTypes.first() == "Landroid/view/ViewStub;"
            } + 1

            addInstructionsWithLabels(
                constIndex,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideAutoPlayButton()Z
                    move-result v$constRegister
                    if-nez v$constRegister, :hidden
                """,
                ExternalLabel("hidden", getInstruction(gotoIndex)),
            )
        }

        // endregion

        // region Hide player control buttons background.

        inflateControlsGroupLayoutStubMethodMatch.let {
            it.method.apply {
                val insertIndex = it.instructionMatches.last().index + 1
                val freeRegister = findFreeRegister(insertIndex)

                addInstructions(
                    insertIndex,
                    """
                        # Move the inflated layout to a temporary register.
                        # The result of the inflate method is by default not moved to a register after the method is called.
                        move-result-object v$freeRegister
                        invoke-static { v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->hidePlayerControlButtonsBackground(Landroid/view/View;)V
                    """,
                )
            }
        }

        // endregion
    }
}
