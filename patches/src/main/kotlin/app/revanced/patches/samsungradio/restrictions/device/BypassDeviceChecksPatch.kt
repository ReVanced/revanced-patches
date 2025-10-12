package app.revanced.patches.samsungradio.restrictions.device

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val bypassDeviceChecksPatch = bytecodePatch(
    name = "Bypass device checks",
    description = "Removes the restriction to use the app on blacklisted phones.",
) {
    compatibleWith("com.sec.android.app.fm")

    execute {
        // Return false = The device is not blacklisted
        checkDeviceFingerprint.method.apply {
            addInstruction(0, "const/4 v0, 0x0")
            addInstruction(1, "return v0")
        }
    }
}
