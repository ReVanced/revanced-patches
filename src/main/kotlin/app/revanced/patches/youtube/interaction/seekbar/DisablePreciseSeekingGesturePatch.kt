package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.AllowSwipingUpGestureFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.ShowSwipingUpGuideFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SwipingUpGestureParentFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.alsoResolve

@Patch(
    name = "Disable precise seeking gesture",
    description = "Adds an option to disable precise seeking when swiping up on the seekbar.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
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
object DisablePreciseSeekingGesturePatch : BytecodePatch(
    setOf(SwipingUpGestureParentFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/DisablePreciseSeekingGesturePatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_disable_precise_seeking_gesture")
        )

        AllowSwipingUpGestureFingerprint.alsoResolve(
            context,
            SwipingUpGestureParentFingerprint
        ).mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->isGestureDisabled()Z
                    move-result v0
                    if-eqz v0, :disabled
                    return-void
                """,
                ExternalLabel("disabled", getInstruction(0))
            )
        }

        ShowSwipingUpGuideFingerprint.alsoResolve(
            context,
            SwipingUpGestureParentFingerprint
        ).mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->isGestureDisabled()Z
                    move-result v0
                    if-eqz v0, :disabled
                    const/4 v0, 0x0
                    return v0
                """,
                ExternalLabel("disabled", getInstruction(0))
            )
        }
    }
}