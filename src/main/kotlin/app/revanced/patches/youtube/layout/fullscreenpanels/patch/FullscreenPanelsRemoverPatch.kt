package app.revanced.patches.youtube.layout.fullscreenpanels.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.data.implementation.proxy
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patches.youtube.layout.fullscreenpanels.annotations.FullscreenPanelsCompatibility

@Patch
@Name("disable-fullscreen-panels")
@Description("Disable comments panel in fullscreen view.")
@FullscreenPanelsCompatibility
@Version("0.0.1")
class FullscreenPanelsRemovalPatch : BytecodePatch(listOf()) {
    override fun execute(data: BytecodeData): PatchResult {
        val classDef = data.classes.first { it.type.endsWith("FullscreenEngagementPanelOverlay;") }
        val method = data.proxy(classDef).resolve().methods.first { it.name == "<init>" }
        val implementation = method.implementation!!

        method.addInstructions(
            implementation.instructions.count() - 1,
            """
			 const/4 v1, 0x0
             iput-boolean v1, v0, ${classDef.type}->a:Z
       		"""
        )

        return PatchResultSuccess()
    }
}
