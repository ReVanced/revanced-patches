package app.revanced.patches.shared.misc.fix.verticalscroll

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val verticalScrollPatch = bytecodePatch(
    description = "Fixes issues with refreshing the feed when the first component is of type EmptyComponent.",
) {
    val canScrollVerticallyMatch by canScrollVerticallyFingerprint()

    execute {
        canScrollVerticallyMatch.mutableMethod.apply {
            val moveResultIndex = canScrollVerticallyMatch.patternMatch!!.endIndex
            val moveResultRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            val insertIndex = moveResultIndex + 1
            addInstruction(
                insertIndex,
                "const/4 v$moveResultRegister, 0x0",
            )
        }
    }
}
