package app.revanced.patches.music.general.castbutton

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.general.castbutton.fingerprints.MediaRouteButtonFingerprint
import app.revanced.patches.music.general.castbutton.fingerprints.PlayerOverlayChipFingerprint
import app.revanced.patches.music.utils.integrations.Constants.GENERAL
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.PlayerOverlayChip
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide cast button",
    description = "Adds an option to hide the cast button.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object HideCastButtonPatch : BytecodePatch(
    setOf(
        MediaRouteButtonFingerprint,
        PlayerOverlayChipFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Hide cast button
         */
        MediaRouteButtonFingerprint.result?.let {
            val setVisibilityMethod =
                it.mutableClass.methods.find { method -> method.name == "setVisibility" }

            setVisibilityMethod?.apply {
                addInstructions(
                    0, """
                        invoke-static {p1}, $GENERAL->hideCastButton(I)I
                        move-result p1
                        """
                )
            } ?: throw PatchException("Failed to find setVisibility method")
        } ?: throw MediaRouteButtonFingerprint.exception

        /**
         * Hide floating cast banner
         */
        PlayerOverlayChipFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralInstructionIndex(PlayerOverlayChip) + 2
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex + 1,
                    "invoke-static {v$targetRegister}, $GENERAL->hideCastButton(Landroid/view/View;)V"
                )
            }
        } ?: throw PlayerOverlayChipFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.GENERAL,
            "revanced_hide_cast_button",
            "true"
        )

    }
}
