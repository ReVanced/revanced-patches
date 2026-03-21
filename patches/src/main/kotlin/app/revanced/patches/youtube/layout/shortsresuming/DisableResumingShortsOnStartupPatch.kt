package app.revanced.patches.youtube.layout.shortsresuming

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_03_or_greater
import app.revanced.patches.youtube.misc.playservice.is_21_03_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableResumingShortsOnStartupPatch;"

@Suppress("unused")
val disableResumingShortsOnStartupPatch = bytecodePatch(
    name = "Disable resuming Shorts on startup",
    description = "Adds an option to disable the Shorts player from resuming on app startup when Shorts were last being watched.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45",
            "20.44.38",
            "20.45.36"
        ),
    )

    apply {
        addResources("youtube", "layout.shortsresuming.disableResumingShortsOnStartupPatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_disable_resuming_shorts_on_startup"),
        )

        if (is_21_03_or_greater) {
            userWasInShortsEvaluateMethodMatch.let {
                it.method.apply {
                    val instruction = getInstruction<RegisterRangeInstruction>(it[0])
                    val zMRegister = instruction.startRegister + 2

                    addInstructions(
                        it[0],
                        """
                            invoke-static { v$zMRegister }, ${EXTENSION_CLASS_DESCRIPTOR}->disableResumingShortsOnStartup(Z)Z
                            move-result v$zMRegister
                        """
                    )
                }
            }
        } else if (is_20_03_or_greater) {
            userWasInShortsListenerMethodMatch.let {
                it.method.apply {
                    val insertIndex = it[2] + 1
                    val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                    addInstructions(
                        insertIndex,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->disableResumingShortsOnStartup(Z)Z
                            move-result v$register
                        """,
                    )
                }
            }
        } else {
            userWasInShortsLegacyMethod.apply {
                val listenableInstructionIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.INVOKE_INTERFACE &&
                            getReference<MethodReference>()?.definingClass == "Lcom/google/common/util/concurrent/ListenableFuture;" &&
                            getReference<MethodReference>()?.name == "isDone"
                }
                val freeRegister = findFreeRegister(listenableInstructionIndex)

                addInstructionsAtControlFlowLabel(
                    listenableInstructionIndex,
                    """
                        invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->disableResumingShortsOnStartup()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :show_startup_shorts_player
                        return-void
                        :show_startup_shorts_player
                        nop
                    """
                )
            }
        }

        userWasInShortsConfigMethod.addInstructions(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->disableResumingShortsOnStartup()Z
                move-result v0
                if-eqz v0, :show
                const/4 v0, 0x0
                return v0
                :show
                nop
            """,
        )
    }
}
