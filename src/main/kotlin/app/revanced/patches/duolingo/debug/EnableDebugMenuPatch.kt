package app.revanced.patches.duolingo.debug

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.duolingo.debug.fingerprints.InitializeBuildConfigProviderFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Enable debug menu",
    compatiblePackages = [CompatiblePackage("com.duolingo", ["5.158.4"])],
    use = false
)
@Suppress("unused")
object EnableDebugMenuPatch : BytecodePatch(
    setOf(InitializeBuildConfigProviderFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        InitializeBuildConfigProviderFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex
                val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    "const/4 v$register, 0x1"
                )
            }
        }
    }
}