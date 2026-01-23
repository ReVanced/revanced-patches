package app.revanced.patches.googlenews.customtabs

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val `Enable CustomTabs` by creatingBytecodePatch(
    description = "Enables CustomTabs to open articles in your default browser.",
) {
    compatibleWith("com.google.android.apps.magazines")

    apply {
        launchCustomTabMethodMatch.method.apply {
            val checkIndex = launchCustomTabMethodMatch.indices.last() + 1
            val register = getInstruction<OneRegisterInstruction>(checkIndex).registerA

            replaceInstruction(checkIndex, "const/4 v$register, 0x1")
        }
    }
}
