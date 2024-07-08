package app.revanced.patches.duolingo.debug

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.duolingo.debug.fingerprints.BuildConfigProviderFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c

@Patch(
    name = "Make Duolingo debug menu available", compatiblePackages = [CompatiblePackage("com.duolingo")], use = false
)
@Suppress("unused")
object MakeDebugMenuAvailable : BytecodePatch(
    setOf(
        BuildConfigProviderFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        BuildConfigProviderFingerprint.result?.mutableMethod?.apply {
            val firstAssigner = getInstructions().filterIsInstance<BuilderInstruction22c>()
                .firstOrNull { it.opcode == Opcode.IPUT_BOOLEAN } ?: throw BuildConfigProviderFingerprint.exception

            // force the value of the first assignment (`isDebugBuild`) to be `true`
            addInstructions(
                firstAssigner.location.index, """
                    const/4 v${firstAssigner.registerA}, 0x1
                """.trimIndent()
            )
        } ?: throw BuildConfigProviderFingerprint.exception
    }
}