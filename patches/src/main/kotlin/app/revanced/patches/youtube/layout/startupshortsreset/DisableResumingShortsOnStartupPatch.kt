package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_02_or_greater
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableResumingStartupShortsPlayerPatch;"

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
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.startupshortsreset.disableResumingShortsOnStartupPatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_disable_resuming_shorts_player"),
        )

        if (is_20_02_or_greater) {
            userWasInShortsAlternativeFingerprint.let {
                it.method.apply {
                    val stringIndex = it.stringMatches!!.first().index
                    val booleanValueIndex = indexOfFirstInstructionReversedOrThrow(stringIndex) {
                        opcode == Opcode.INVOKE_VIRTUAL &&
                                getReference<MethodReference>()?.name == "booleanValue"
                    }
                    val booleanValueRegister =
                        getInstruction<OneRegisterInstruction>(booleanValueIndex + 1).registerA

                    addInstructions(
                        booleanValueIndex + 2, """
                            invoke-static {v$booleanValueRegister}, $EXTENSION_CLASS_DESCRIPTOR->disableResumingStartupShortsPlayer(Z)Z
                            move-result v$booleanValueRegister
                            """
                    )
                }
            }
        } else {
            userWasInShortsLegacyFingerprint.method.apply {
                val listenableInstructionIndex = indexOfFirstInstructionOrThrow {
                    val reference = getReference<MethodReference>()
                    opcode == Opcode.INVOKE_INTERFACE &&
                            reference?.definingClass == "Lcom/google/common/util/concurrent/ListenableFuture;" &&
                            reference.name == "isDone"
                }
                val freeRegister = findFreeRegister(listenableInstructionIndex)

                addInstructionsAtControlFlowLabel(
                    listenableInstructionIndex,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->disableResumingStartupShortsPlayer()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :show
                        return-void
                        :show
                        nop
                    """
                )
            }
        }

        userWasInShortsConfigFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->disableResumingStartupShortsPlayer()Z
                move-result v0
                if-eqz v0, :show
                const/4 v0, 0x0
                return v0
                :show
                nop
            """
        )
    }
}
