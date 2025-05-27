package app.revanced.patches.youtube.player.overlaybuttons

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.layoutConstructorFingerprint
import app.revanced.patches.youtube.shared.subtitleButtonControllerFingerprint
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var playerControlPreviousButtonTouchArea = -1L
    private set
internal var playerControlNextButtonTouchArea = -1L
    private set

private val hidePlayerOverlayButtonsResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        playerControlPreviousButtonTouchArea = resourceMappings["id", "player_control_previous_button_touch_area"]
        playerControlNextButtonTouchArea = resourceMappings["id", "player_control_next_button_touch_area"]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HidePlayerOverlayButtonsPatch;"

val hidePlayerOverlayButtonsPatch = bytecodePatch(
    name = "Hide player overlay buttons",
    description = "Adds options to hide the player Cast, Autoplay, Captions, and Previous & Next buttons.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        hidePlayerOverlayButtonsResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    execute {
        addResources("youtube", "player.overlaybuttons.hidePlayerOverlayButtonsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_player_previous_next_buttons"),
            SwitchPreference("revanced_hide_cast_button"),
            SwitchPreference("revanced_hide_captions_button"),
            SwitchPreference("revanced_hide_autoplay_button"),
        )

        // region Hide player next/previous button.

        playerControlsPreviousNextOverlayTouchFingerprint.method.apply {
            val resourceIndex = indexOfFirstLiteralInstructionOrThrow(playerControlPreviousButtonTouchArea)

            val insertIndex = indexOfFirstInstructionOrThrow(resourceIndex) {
                opcode == Opcode.INVOKE_STATIC &&
                    getReference<MethodReference>()?.parameterTypes?.firstOrNull() == "Landroid/view/View;"
            }

            val viewRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

            addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, $EXTENSION_CLASS_DESCRIPTOR" +
                    "->hidePreviousNextButtons(Landroid/view/View;)V",
            )
        }

        // endregion

        // region Hide cast button.

        mediaRouteButtonFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->getCastButtonOverrideV2(I)I
                move-result p1
            """,
        )

        // endregion

        // region Hide captions button.

        subtitleButtonControllerFingerprint.method.apply {
            // Due to previously applied patches, scanResult index cannot be used in this context
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_BOOLEAN) + 1

            addInstruction(
                insertIndex,
                "invoke-static {v0}, $EXTENSION_CLASS_DESCRIPTOR->hideCaptionsButton(Landroid/widget/ImageView;)V",
            )
        }

        // endregion

        // region Hide autoplay button.

        layoutConstructorFingerprint.method.apply {
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
    }
}
