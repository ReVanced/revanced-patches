package app.revanced.patches.youtube.player.captionsbutton

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.player.captionsbutton.fingerprints.LithoSubtitleButtonConfigFingerprint
import app.revanced.patches.youtube.player.captionsbutton.fingerprints.YouTubeControlsOverlaySubtitleButtonFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.PLAYER
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide captions button",
    description = "Adds an option to hide the captions button in the video player.",
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object HideCaptionsButtonBytecodePatch : BytecodePatch(
    setOf(
        LithoSubtitleButtonConfigFingerprint,
        YouTubeControlsOverlaySubtitleButtonFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Added in YouTube v18.31.40
         *
         * No exception even if fail to resolve fingerprints.
         * For compatibility with YouTube v18.25.40 ~ YouTube v18.30.37.
         */
        LithoSubtitleButtonConfigFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $PLAYER->hideCaptionsButton(Z)Z
                        move-result v$insertRegister
                        """
                )
            }
        }

        YouTubeControlsOverlaySubtitleButtonFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static {v$insertRegister}, $PLAYER->hideCaptionsButton(Landroid/view/View;)V"
                )
            }
        } ?: throw YouTubeControlsOverlaySubtitleButtonFingerprint.exception

        LithoFilterPatch.addFilter("$COMPONENTS_PATH/CaptionsFilter;")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: HIDE_CAPTIONS_BUTTON"
            )
        )

        SettingsPatch.updatePatchStatus("Hide captions button")

    }
}