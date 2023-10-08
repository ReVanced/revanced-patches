package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.IsSwipingUpFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Disable fine scrubbing gesture",
    description = "Disables gesture that shows the fine scrubbing overlay when swiping up on the seekbar.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44"
            ]
        )
    ]
)
@Suppress("unused")
object DisableFineScrubbingGesturePatch : BytecodePatch(
    setOf(IsSwipingUpFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/DisableFineScrubbingGesturePatch;->" +
                "disableGesture(Landroid/view/VelocityTracker;Landroid/view/MotionEvent;)V"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            SwitchPreference(
                "revanced_disable_fine_scrubbing_gesture",
                StringResource("revanced_disable_fine_scrubbing_gesture_title", "Disable fine scrubbing gesture"),
                StringResource("revanced_disable_fine_scrubbing_gesture_summary_on", "Gesture is disabled"),
                StringResource("revanced_disable_fine_scrubbing_gesture_summary_off", "Gesture is enabled"),
            )
        )

        IsSwipingUpFingerprint.result?.let {
            val addMovementIndex = it.scanResult.patternScanResult!!.startIndex - 1

            it.mutableMethod.apply {
                val addMovementInstruction = getInstruction<FiveRegisterInstruction>(addMovementIndex)
                val trackerRegister = addMovementInstruction.registerC
                val eventRegister = addMovementInstruction.registerD

                replaceInstruction(
                    addMovementIndex,
                    "invoke-static {v$trackerRegister, v$eventRegister}, $INTEGRATIONS_METHOD_DESCRIPTOR"
                )
            }
        } ?: throw IsSwipingUpFingerprint.exception
    }
}