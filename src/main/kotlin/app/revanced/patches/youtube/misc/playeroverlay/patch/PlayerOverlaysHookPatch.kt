package app.revanced.patches.youtube.misc.playeroverlay.patch

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
import app.revanced.patches.youtube.misc.playeroverlay.annotation.PlayerOverlaysHookCompatibility
import app.revanced.patches.youtube.misc.playeroverlay.fingerprint.PlayerOverlaysOnFinishInflateFingerprint

@Name("player-overlays-hook")
@Description("Hook for adding custom overlays to the video player.")
@PlayerOverlaysHookCompatibility
@Version("0.0.1")
@Dependencies([IntegrationsPatch::class])
class PlayerOverlaysHookPatch : BytecodePatch(
    listOf(
        PlayerOverlaysOnFinishInflateFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // hook YouTubePlayerOverlaysLayout.onFinishInflate()
        val method = PlayerOverlaysOnFinishInflateFingerprint.result!!.mutableMethod
        method.addInstruction(
            method.implementation!!.instructions.size - 2,
            "invoke-static { p0 }, Lapp/revanced/integrations/patches/PlayerOverlaysHookPatch;->YouTubePlayerOverlaysLayout_onFinishInflateHook(Ljava/lang/Object;)V"
        )
        return PatchResultSuccess()
    }
}