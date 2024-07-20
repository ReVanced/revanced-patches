package app.revanced.patches.googlenews.customtabs

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.googlenews.customtabs.fingerprints.LaunchCustomTabFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable CustomTabs",
    description = "Enables CustomTabs to open articles in your default browser.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.magazines")],
)
@Suppress("unused")
object EnableCustomTabs : BytecodePatch(
    setOf(LaunchCustomTabFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        LaunchCustomTabFingerprint.resultOrThrow().let { result ->
            result.mutableMethod.apply {
                val checkIndex = result.scanResult.patternScanResult!!.endIndex + 1
                val register = getInstruction<OneRegisterInstruction>(checkIndex).registerA

                replaceInstruction(checkIndex, "const/4 v$register, 0x1")
            }
        }
    }
}
