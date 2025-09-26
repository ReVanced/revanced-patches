package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val enableDevMenuPatch = bytecodePatch(
    name = "Enable dev menu",
    description = "Hides posts and reels from the explore/search page.", //TODO
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        devMenuFingerprint.method.returnEarly(true)
    }
}

