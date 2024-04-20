package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Disable resuming Shorts on startup",
    description = "Adds an option to disable the Shorts player from resuming on app startup when Shorts were last being watched.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
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
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object DisableResumingShortsOnStartupPatch : BytecodePatch(
    setOf(UserWasInShortsFingerprint)
) {

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/DisableResumingStartupShortsPlayerPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_disable_resuming_shorts_player")
        )

        UserWasInShortsFingerprint.result?.mutableMethod?.apply {
            val listenableInstructionIndex = indexOfFirstInstruction {
                opcode == Opcode.INVOKE_INTERFACE &&
                        getReference<MethodReference>()?.definingClass == "Lcom/google/common/util/concurrent/ListenableFuture;" &&
                        getReference<MethodReference>()?.name == "isDone"
            }
            if (listenableInstructionIndex < 0) throw PatchException("Could not find instruction index")
            val originalInstructionRegister = getInstruction<FiveRegisterInstruction>(listenableInstructionIndex).registerC
            val freeRegister = getInstruction<OneRegisterInstruction>(listenableInstructionIndex + 1).registerA

            // Replace original instruction to preserve control flow label.
            replaceInstruction(
                listenableInstructionIndex,
                "invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->disableResumingStartupShortsPlayer()Z"
            )
            addInstructionsWithLabels(
                listenableInstructionIndex + 1,
                """
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :show_startup_shorts_player
                    return-void
                    :show_startup_shorts_player
                    invoke-interface {v$originalInstructionRegister}, Lcom/google/common/util/concurrent/ListenableFuture;->isDone()Z
                """
            )
        } ?: throw UserWasInShortsFingerprint.exception
    }
}
