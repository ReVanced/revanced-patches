package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import java.util.logging.Logger

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/instagram/hide/navigation/HideNavigationButtonsPatch;"

@Suppress("unused")
val hideNavigationButtonsPatch = bytecodePatch(
    name = "Hide navigation buttons",
    description = "Hides navigation bar buttons, such as the Reels and Create button.",
    use = false
) {
    compatibleWith("com.instagram.android"("397.1.0.52.81"))

    val hideReels by booleanOption(
        key = "hideReels",
        default = true,
        title = "Hide Reels",
        description = "Permanently hides the Reels button."
    )

    val hideCreate by booleanOption(
        key = "hideCreate",
        default = true,
        title = "Hide Create",
        description = "Permanently hides the Create button."
    )

    execute {
        if (!hideReels!! && !hideCreate!!) {
            return@execute Logger.getLogger(this::class.java.name).warning(
                "No hide navigation buttons options are enabled. No changes made."
            )
        }

        initializeTabArrayFingerprint.method.apply {
            val returnIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
            val tabListRegister = getInstruction<OneRegisterInstruction>(returnIndex).registerA
            val freeRegister = findFreeRegister(returnIndex)

            fun instructionsRemoveTabByName(tabName: String): String {
                return """
                    const-string v$freeRegister, "$tabName"
                    invoke-static { v$tabListRegister, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->removeNavigationTabByName(Ljava/util/List;Ljava/lang/String;)Ljava/util/List;
                    move-result-object v$tabListRegister
                    """
            }

            if (hideReels!!)
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveTabByName("fragment_clips")
                )

            if (hideCreate!!)
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveTabByName("fragment_share")
                )
        }
    }
}
