package app.revanced.patches.youtube.interaction.swipecontrols.patch.bytecode

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility
import app.revanced.patches.youtube.interaction.swipecontrols.fingerprints.WatchWhileOnStartFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.patch.resource.SwipeControlsResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playeroverlay.patch.PlayerOverlaysHookPatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch

@Patch
@Name("swipe-controls")
@Description("Adds volume and brightness swipe controls.")
@SwipeControlsCompatibility
@Version("0.0.2")
@Dependencies(
    dependencies = [
        IntegrationsPatch::class,
        PlayerTypeHookPatch::class,
        PlayerOverlaysHookPatch::class,
        SwipeControlsResourcePatch::class
    ]
)
class SwipeControlsBytecodePatch : BytecodePatch(
    listOf(
        WatchWhileOnStartFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        WatchWhileOnStartFingerprint.result!!.mutableMethod.addInstruction(
            0,
            "invoke-static { p0 }, Lapp/revanced/integrations/patches/SwipeControlsPatch;->WatchWhileActivity_onStartHookEX(Ljava/lang/Object;)V"
        )
        return PatchResultSuccess()
    }
}