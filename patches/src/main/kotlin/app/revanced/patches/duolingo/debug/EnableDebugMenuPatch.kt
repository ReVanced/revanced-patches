package app.revanced.patches.duolingo.debug

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val enableDebugMenuPatch = bytecodePatch(
    name = "Enable debug menu",
    use = false,
) {
    compatibleWith("com.duolingo"("5.158.4"))

    val initializeBuildConfigProviderMatch by initializeBuildConfigProviderFingerprint()

    execute {
        initializeBuildConfigProviderMatch.mutableMethod.apply {
            val insertIndex = initializeBuildConfigProviderMatch.patternMatch!!.startIndex
            val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

            addInstructions(
                insertIndex,
                "const/4 v$register, 0x1",
            )
        }
    }
}
