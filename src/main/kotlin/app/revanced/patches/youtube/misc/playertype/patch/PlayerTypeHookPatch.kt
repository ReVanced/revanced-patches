package app.revanced.patches.youtube.misc.playertype.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.annotation.PlayerTypeHookCompatibility
import app.revanced.patches.youtube.misc.playertype.fingerprint.UpdatePlayerTypeFingerprint

@Name("player-type-hook")
@Description("hook to get the current player type of WatchWhileActivity")
@PlayerTypeHookCompatibility
@Version("0.0.1")
@Dependencies(dependencies = [IntegrationsPatch::class])
class PlayerTypeHookPatch : BytecodePatch(
    listOf(
        UpdatePlayerTypeFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // hook YouTubePlayerOverlaysLayout.updatePlayerLayout()
        UpdatePlayerTypeFingerprint.result!!.mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, Lapp/revanced/integrations/patches/PlayerTypeHookPatch;->YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(Ljava/lang/Object;)V"
        )
        return PatchResultSuccess()
    }
}
