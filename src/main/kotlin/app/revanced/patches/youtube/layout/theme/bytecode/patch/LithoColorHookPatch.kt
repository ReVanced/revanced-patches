package app.revanced.patches.youtube.layout.theme.bytecode.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.theme.annotations.ThemeCompatibility
import app.revanced.patches.youtube.layout.theme.bytecode.fingerprints.LithoThemeFingerprint

@Name("Litho color hook")
@Description("Adds a hook to set color of Litho components.")
@ThemeCompatibility
class LithoColorHookPatch : BytecodePatch(listOf(LithoThemeFingerprint)) {
    override fun execute(context: BytecodeContext) {
        LithoThemeFingerprint.result?.let {
            insertionIndex = it.scanResult.patternScanResult!!.endIndex - 1
            colorRegister = "p1"
            insertionMethod = it.mutableMethod
        } ?: throw LithoThemeFingerprint.exception
    }
    companion object {
        private var insertionIndex : Int = -1
        private lateinit var colorRegister : String
        private lateinit var insertionMethod : MutableMethod

        internal fun lithoColorOverrideHook(targetMethodClass: String, targetMethodName: String)  {
            insertionMethod.addInstructions(
                insertionIndex,
                """
                    invoke-static {$colorRegister}, $targetMethodClass->$targetMethodName(I)I
                    move-result $colorRegister
                """
            )
            insertionIndex += 2
        }
    }

}