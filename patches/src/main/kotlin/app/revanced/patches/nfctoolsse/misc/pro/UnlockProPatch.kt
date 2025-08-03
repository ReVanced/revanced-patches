package app.revanced.patches.nfctoolsse.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
@Deprecated("This patch no longer works and will soon be deleted.")
val unlockProPatch = bytecodePatch{
    compatibleWith("com.wakdev.apps.nfctools.se")

    execute {
        isLicenseRegisteredFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
