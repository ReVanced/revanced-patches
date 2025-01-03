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

    execute {
        initializeBuildConfigProviderFingerprint.method.apply {
            val insertIndex = initializeBuildConfigProviderFingerprint.filterMatches.first().index
            val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

            addInstructions(
                insertIndex,
                "const/4 v$register, 0x1",
            )
        }
    }
}
