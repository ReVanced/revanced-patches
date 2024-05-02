package app.revanced.patches.magazines.misc.customtabs

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.magazines.misc.gms.customtabs.fingerprints.UseCustomTabsFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable CustomTabs",
    description = "Enables CustomTabs which allows the Articles to be opened in your set Default Browser instead of Chrome.",
    dependencies = [],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.magazines")]
)
@Suppress("unused")
object UseCustomTabs : BytecodePatch(setOf(UseCustomTabsFingerprint)) {
    override fun execute(context: BytecodeContext) = UseCustomTabsFingerprint.result?.let { result ->
        val cmpIndex = result.scanResult.patternScanResult!!.endIndex + 1
        val cmpResultRegister = result.mutableMethod.getInstruction<OneRegisterInstruction>(cmpIndex).registerA
        result.mutableMethod.replaceInstruction(cmpIndex, "const/4 v${cmpResultRegister}, 0x1")
    } ?: throw UseCustomTabsFingerprint.exception
}
