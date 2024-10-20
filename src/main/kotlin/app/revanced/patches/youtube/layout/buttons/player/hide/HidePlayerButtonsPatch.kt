package app.revanced.patches.youtube.layout.buttons.player.hide

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.player.hide.fingerprints.PlayerControlsPreviousNextOverlayTouchFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide player buttons",
    description = "Adds an option to hide the previous and next buttons in the video player.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        HidePlayerButtonsResourcePatch::class,
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
object HidePlayerButtonsPatch : BytecodePatch(
    setOf(PlayerControlsPreviousNextOverlayTouchFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_player_buttons")
        )

        PlayerControlsPreviousNextOverlayTouchFingerprint.resultOrThrow().mutableMethod.apply {
            val resourceIndex = indexOfFirstWideLiteralInstructionValueOrThrow(
                HidePlayerButtonsResourcePatch.playerControlPreviousButtonTouchArea
            )

            val insertIndex = indexOfFirstInstructionOrThrow(resourceIndex) {
                opcode == Opcode.INVOKE_STATIC
                        && getReference<MethodReference>()?.parameterTypes?.firstOrNull() == "Landroid/view/View;"
            }

            val viewRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

            addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, Lapp/revanced/integrations/youtube/patches/HidePlayerButtonsPatch;" +
                        "->hidePreviousNextButtons(Landroid/view/View;)V"
            )
        }
    }
}
