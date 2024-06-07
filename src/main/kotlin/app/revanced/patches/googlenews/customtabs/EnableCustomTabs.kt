package app.revanced.patches.googlenews.customtabs

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.googlenews.customtabs.fingerprints.LaunchCustomTabFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable CustomTabs",
    description = "Enables CustomTabs which allows articles to be opened in your default browser instead of Chrome.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.magazines")],
)
@Suppress("unused")
object EnableCustomTabs : BytecodePatch(setOf(LaunchCustomTabFingerprint)) {
    override fun execute(context: BytecodeContext) = LaunchCustomTabFingerprint.result?.let { result ->
        val checkIndex = result.scanResult.patternScanResult!!.endIndex + 1

        result.mutableMethod.apply {
            val register = getInstruction<OneRegisterInstruction>(checkIndex).registerA

            replaceInstruction(checkIndex, "const/4 v$register, 0x1")
        }

        return@let
    } ?: throw LaunchCustomTabFingerprint.exception
}
