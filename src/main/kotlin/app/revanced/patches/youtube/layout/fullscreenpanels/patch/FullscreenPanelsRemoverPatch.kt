package app.revanced.patches.youtube.layout.fullscreenpanels.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.data.impl.proxy
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
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
