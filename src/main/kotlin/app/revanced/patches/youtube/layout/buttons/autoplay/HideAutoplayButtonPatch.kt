package app.revanced.patches.youtube.layout.buttons.autoplay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.LayoutConstructorFingerprint
import app.revanced.util.exception
import app.revanced.util.indexOfIdResourceOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide autoplay button",
    description = "Adds an option to hide the autoplay button in the video player.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    ],
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
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
            ],
        ),
    ],
)
@Suppress("unused")
object HideAutoplayButtonPatch : BytecodePatch(
    setOf(LayoutConstructorFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_autoplay_button"),
        )

        LayoutConstructorFingerprint.result?.mutableMethod?.apply {
            val layoutGenMethodInstructions = implementation!!.instructions

            // resolve the offsets of where to insert the branch instructions and ...
            val insertIndex = indexOfIdResourceOrThrow("autonav_preview_stub")

            // where to branch away
            val branchIndex =
                layoutGenMethodInstructions.subList(insertIndex + 1, layoutGenMethodInstructions.size - 1)
                    .indexOfFirst {
                        ((it as? ReferenceInstruction)?.reference as? MethodReference)?.name == "addOnLayoutChangeListener"
                    } + 2

            val jumpInstruction = layoutGenMethodInstructions[insertIndex + branchIndex] as Instruction

            // can be clobbered because this register is overwritten after the injected code
            val clobberRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            addInstructionsWithLabels(
                insertIndex,
                """
                    invoke-static {}, Lapp/revanced/integrations/youtube/patches/HideAutoplayButtonPatch;->isButtonShown()Z
                    move-result v$clobberRegister
                    if-eqz v$clobberRegister, :hidden
                """,
                ExternalLabel("hidden", jumpInstruction),
            )
        } ?: throw LayoutConstructorFingerprint.exception
    }
}
