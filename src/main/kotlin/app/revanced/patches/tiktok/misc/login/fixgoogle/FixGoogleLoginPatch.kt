package app.revanced.patches.tiktok.misc.login.fixgoogle

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tiktok.misc.login.fixgoogle.fingerprints.GoogleAuthAvailableFingerprint
import app.revanced.patches.tiktok.misc.login.fixgoogle.fingerprints.GoogleOneTapAuthAvailableFingerprint

@Patch(
    name = "Fix Google login",
    description = "Allows logging in with a Google account.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill"),
        CompatiblePackage("com.zhiliaoapp.musically")
    ]
)
@Suppress("unused")
object FixGoogleLoginPatch : BytecodePatch(
    setOf(GoogleOneTapAuthAvailableFingerprint, GoogleAuthAvailableFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        listOf(
            GoogleOneTapAuthAvailableFingerprint,
            GoogleAuthAvailableFingerprint
        ).forEach {
            with(it.result!!.mutableMethod) {
                addInstructions(
                    0,
                    """
                        const/4 v0, 0x0
                        return v0
                    """
                )
            }
        }
    }
}