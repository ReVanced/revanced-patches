package app.revanced.patches.tiktok.misc.loginfallback.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.misc.loginfallback.annotations.TikTokWebLoginCompatibility
import app.revanced.patches.tiktok.misc.loginfallback.fingerprints.GoogleAuthAvailableFingerprint
import app.revanced.patches.tiktok.misc.loginfallback.fingerprints.GoogleOneTapAuthAvailableFingerprint

@Patch
@Name("tiktok-web-login")
@Description("Allows logging in with a Google account.")
@TikTokWebLoginCompatibility
@Version("0.0.1")
class TikTokLoginFallbackPatch : BytecodePatch(
    listOf(
        GoogleOneTapAuthAvailableFingerprint,
        GoogleAuthAvailableFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
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
        return PatchResultSuccess()
    }
}