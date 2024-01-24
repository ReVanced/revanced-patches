package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.util.exception
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
    name = "Disable precise seeking gesture",
    description = "Adds an option to disable precise seeking when swiping up on the seekbar.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.35"
            ]
        )
    ]
)
@Suppress("unused")
object DisablePreciseSeekingGesturePatch : BytecodePatch(
    setOf(IsSwipingUpFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/DisablePreciseSeekingGesturePatch;->" +
                "disableGesture(Landroid/view/VelocityTracker;Landroid/view/MotionEvent;)V"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            SwitchPreference(
                "revanced_disable_precise_seeking_gesture",
                StringResource("revanced_disable_precise_seeking_gesture_title", "Disable precise seeking gesture"),
                StringResource("revanced_disable_precise_seeking_gesture_summary_on", "Gesture is disabled"),
                StringResource("revanced_disable_precise_seeking_gesture_summary_off", "Gesture is enabled"),
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