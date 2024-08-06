package app.revanced.patches.duolingo.debug

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.duolingo.debug.fingerprints.InitializeBuildConfigProviderFingerprint
import app.revanced.util.exception
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c

@Patch(
    name = "Enable debug menu",
    compatiblePackages = [CompatiblePackage("com.duolingo")],
    use = false
)
@Suppress("unused")
object EnableDebugMenuPatch : BytecodePatch(
    setOf(InitializeBuildConfigProviderFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        InitializeBuildConfigProviderFingerprint.resultOrThrow().mutableMethod.apply {
            val setIsDebugBuildIndex = getInstructions().firstOrNull {
                it.opcode == Opcode.IPUT_BOOLEAN
            } as? BuilderInstruction22c ?: throw InitializeBuildConfigProviderFingerprint.exception

            addInstructions(
                setIsDebugBuildIndex.location.index,
                "const/4 v${setIsDebugBuildIndex.registerA}, 0x1"
            )
        }
    }
}