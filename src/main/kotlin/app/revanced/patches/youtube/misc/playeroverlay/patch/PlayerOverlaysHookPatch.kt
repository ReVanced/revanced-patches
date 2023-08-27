package app.revanced.patches.youtube.misc.playeroverlay.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playeroverlay.annotation.PlayerOverlaysHookCompatibility
import app.revanced.patches.youtube.misc.playeroverlay.fingerprint.PlayerOverlaysOnFinishInflateFingerprint

@Name("Player overlays hook")
@Description("Hook for adding custom overlays to the video player.")
@PlayerOverlaysHookCompatibility
@DependsOn([IntegrationsPatch::class])
class PlayerOverlaysHookPatch : BytecodePatch(
    listOf(
        PlayerOverlaysOnFinishInflateFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        // hook YouTubePlayerOverlaysLayout.onFinishInflate()
        val method = PlayerOverlaysOnFinishInflateFingerprint.result!!.mutableMethod
        method.addInstruction(
            method.implementation!!.instructions.size - 2,
            "invoke-static { p0 }, Lapp/revanced/integrations/patches/PlayerOverlaysHookPatch;->YouTubePlayerOverlaysLayout_onFinishInflateHook(Ljava/lang/Object;)V"
        )
    }
}