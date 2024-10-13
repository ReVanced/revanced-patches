package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableResumingStartupShortsPlayerPatch;"

@Suppress("unused")
val disableResumingShortsOnStartupPatch = bytecodePatch(
    name = "Disable resuming Shorts on startup",
    description = "Adds an option to disable the Shorts player from resuming on app startup when Shorts were last being watched.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val userWasInShortsMatch by userWasInShortsFingerprint()

    execute {
        addResources("youtube", "layout.startupshortsreset.disableResumingShortsOnStartupPatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_disable_resuming_shorts_player"),
        )

        userWasInShortsMatch.mutableMethod.apply {
            val listenableInstructionIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_INTERFACE &&
                    getReference<MethodReference>()?.definingClass == "Lcom/google/common/util/concurrent/ListenableFuture;" &&
                    getReference<MethodReference>()?.name == "isDone"
            }
            val originalInstructionRegister = getInstruction<FiveRegisterInstruction>(listenableInstructionIndex).registerC
            val freeRegister = getInstruction<OneRegisterInstruction>(listenableInstructionIndex + 1).registerA

            // Replace original instruction to preserve control flow label.
            replaceInstruction(
                listenableInstructionIndex,
                "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->disableResumingStartupShortsPlayer()Z",
            )
            addInstructionsWithLabels(
                listenableInstructionIndex + 1,
                """
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :show_startup_shorts_player
                    return-void
                    :show_startup_shorts_player
                    invoke-interface {v$originalInstructionRegister}, Lcom/google/common/util/concurrent/ListenableFuture;->isDone()Z
                """,
            )
        }
    }
}
