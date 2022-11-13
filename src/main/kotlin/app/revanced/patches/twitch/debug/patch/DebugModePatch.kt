package app.revanced.patches.twitch.debug.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.twitch.debug.annotations.DebugModeCompatibility
import app.revanced.patches.twitch.debug.fingerprints.IsDebugConfigEnabledFingerprint
import app.revanced.patches.twitch.debug.fingerprints.IsOmVerificationEnabledFingerprint
import app.revanced.patches.twitch.debug.fingerprints.ShouldShowDebugOptionsFingerprint

@Patch(false)
@Name("debug-mode")
@Description("Enables Twitch's internal debugging mode.")
@DebugModeCompatibility
@Version("0.0.1")
class DebugModePatch : BytecodePatch(
    listOf(
        IsDebugConfigEnabledFingerprint,
        IsOmVerificationEnabledFingerprint,
        ShouldShowDebugOptionsFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        listOf(
            IsDebugConfigEnabledFingerprint,
            IsOmVerificationEnabledFingerprint,
            ShouldShowDebugOptionsFingerprint
        ).forEach {
            with(it.result!!) {
                with(mutableMethod) {
                    addInstructions(
                        0,
                        """
                             const/4 v0, 0x1
                             return v0
                          """
                    )
                }
            }
        }
        return PatchResultSuccess()
    }
}
