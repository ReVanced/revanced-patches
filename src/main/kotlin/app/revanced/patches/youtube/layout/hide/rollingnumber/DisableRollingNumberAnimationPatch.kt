package app.revanced.patches.youtube.layout.hide.rollingnumber

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.RollingNumberTextViewAnimationUpdateFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Disable rolling number animations",
    description = "Adds an option to disable rolling number animations of video view count, user likes, and upload time.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                // 18.43 is the earliest target this patch works.
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object DisableRollingNumberAnimationPatch : BytecodePatch(
    setOf(
        RollingNumberTextViewAnimationUpdateFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/DisableRollingNumberAnimationsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_rolling_number_animations")
        )

        // Animations are disabled by preventing an Image from being applied to the text span,
        // which prevents the animations from appearing.
        RollingNumberTextViewAnimationUpdateFingerprint.result?.apply {
            val patternScanResult = scanResult.patternScanResult!!
            val blockStartIndex = patternScanResult.startIndex
            val blockEndIndex = patternScanResult.endIndex + 1
            mutableMethod.apply {
                val freeRegister = getInstruction<OneRegisterInstruction>(blockStartIndex).registerA

                // ReturnYouTubeDislike also makes changes to this same method,
                // and must add control flow label to a noop instruction to
                // ensure RYD patch adds it's changes after the control flow label.
                addInstructions(blockEndIndex, "nop")

                addInstructionsWithLabels(
                    blockStartIndex,
                    """
                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->disableRollingNumberAnimations()Z
                        move-result v$freeRegister
                        if-nez v$freeRegister, :disable_animations
                    """,
                    ExternalLabel("disable_animations", getInstruction(blockEndIndex))
                )
            }
        } ?: throw RollingNumberTextViewAnimationUpdateFingerprint.exception
    }
}
