package app.revanced.patches.youtube.layout.buttons.player

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.autoplay.HideAutoplayButtonPatch
import app.revanced.patches.youtube.layout.buttons.player.fingerprints.PlayerControlsPreviousNextOverlayTouchFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.LayoutConstructorFingerprint
import app.revanced.patches.youtube.shared.fingerprints.SubtitleButtonControllerFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.indexOfIdResourceOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide player overlay buttons",
    description = "Adds options to hide the player cast button, autoplay button, caption button, next/previous buttons",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        HidePlayerOverlayButtonsResourcePatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object HidePlayerOverlayButtonsPatch : BytecodePatch(
    setOf(
        PlayerControlsPreviousNextOverlayTouchFingerprint,
        SubtitleButtonControllerFingerprint,
        LayoutConstructorFingerprint
    )
) {

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/HidePlayerOverlayButtonsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_player_buttons"),
            SwitchPreference("revanced_hide_cast_button"),
            SwitchPreference("revanced_hide_autoplay_button"),
        )

        // region hide player next/previous button

        PlayerControlsPreviousNextOverlayTouchFingerprint.resultOrThrow().mutableMethod.apply {
            val resourceIndex = indexOfFirstWideLiteralInstructionValueOrThrow(
                HidePlayerOverlayButtonsResourcePatch.playerControlPreviousButtonTouchArea
            )

            val insertIndex = indexOfFirstInstructionOrThrow(resourceIndex) {
                opcode == Opcode.INVOKE_STATIC
                        && getReference<MethodReference>()?.parameterTypes?.firstOrNull() == "Landroid/view/View;"
            }

            val viewRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

            addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR" +
                        "->hidePreviousNextButtons(Landroid/view/View;)V"
            )
        }

        // endregion

        // region hide cast button

        val buttonClass = context.findClass("MediaRouteButton")
            ?: throw PatchException("MediaRouteButton class not found.")

        buttonClass.mutableClass.methods.find { it.name == "setVisibility" }?.apply {
            addInstructions(
                0,
                """
                    invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->getCastButtonOverrideV2(I)I
                    move-result p1
                """,
            )
        } ?: throw PatchException("setVisibility method not found.")

        // endregion

        // region hide captions button

        SubtitleButtonControllerFingerprint.resultOrThrow().mutableMethod.apply {
            // Due to previously applied patches, scanResult index cannot be used in this context
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_BOOLEAN) + 1

            addInstruction(
                insertIndex,
                "invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->hideCaptionsButton(Landroid/widget/ImageView;)V"
            )
        }

        // endregion

        // region hide auto play button

        LayoutConstructorFingerprint.resultOrThrow().mutableMethod.apply {
            val constIndex = indexOfIdResourceOrThrow("autonav_toggle")
            val constRegister = getInstruction<OneRegisterInstruction>(constIndex).registerA

            // Add a conditional branch around the code that inflates and adds the auto repeat button.
            val gotoIndex = indexOfFirstInstructionOrThrow(constIndex) {
                val parameterTypes = getReference<MethodReference>()?.parameterTypes
                opcode == Opcode.INVOKE_VIRTUAL &&
                        parameterTypes?.size == 2 &&
                        parameterTypes.first() == "Landroid/view/ViewStub;"
            } + 1

            addInstructionsWithLabels(
                constIndex,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->hideAutoPlayButton()Z
                    move-result v$constRegister
                    if-nez v$constRegister, :hidden
                """,
                ExternalLabel("hidden", getInstruction(gotoIndex)),
            )
        }

        // endregion
    }
}
