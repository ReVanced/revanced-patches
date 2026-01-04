package app.revanced.patches.shared.misc.fix.verticalscroll

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val verticalScrollPatch = bytecodePatch(
    description = "Fixes issues with refreshing the feed when the first component is of type EmptyComponent.",
) {

    execute {
        canScrollVerticallyFingerprint.let {
            it.method.apply {
                val moveResultIndex = it.instructionMatches.last().index
                val moveResultRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

                val insertIndex = moveResultIndex + 1
                addInstruction(
                    insertIndex,
                    "const/4 v$moveResultRegister, 0x0",
                )
            }
        }
    }
}
