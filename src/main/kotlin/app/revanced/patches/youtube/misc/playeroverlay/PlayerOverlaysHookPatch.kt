package app.revanced.patches.youtube.misc.playeroverlay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playeroverlay.fingerprint.PlayerOverlaysOnFinishInflateFingerprint
import app.revanced.util.exception

@Patch(
    description = "Hook for the video player overlay",
    dependencies = [IntegrationsPatch::class],
)

/**
 * Edit: This patch is not in use and may not work.
 */
@Suppress("unused")
object PlayerOverlaysHookPatch : BytecodePatch(
    setOf(PlayerOverlaysOnFinishInflateFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/PlayerOverlaysHookPatch;"

    override fun execute(context: BytecodeContext) {
        PlayerOverlaysOnFinishInflateFingerprint.result?.mutableMethod?.apply {
            addInstruction(
                implementation!!.instructions.lastIndex,
                "invoke-static { p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->playerOverlayInflated(Landroid/view/ViewGroup;)V"
            )
        } ?: throw PlayerOverlaysOnFinishInflateFingerprint.exception
    }
}