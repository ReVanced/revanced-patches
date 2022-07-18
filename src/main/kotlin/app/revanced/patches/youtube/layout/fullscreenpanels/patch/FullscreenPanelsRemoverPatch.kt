package app.revanced.patches.youtube.layout.fullscreenpanels.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.fullscreenpanels.annotations.FullscreenPanelsCompatibility
import app.revanced.patches.youtube.layout.fullscreenpanels.fingerprints.FullscreenViewAdderFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@Name("disable-fullscreen-panels")
@Dependencies([IntegrationsPatch::class])
@Description("Disables video description and comments panel in fullscreen view.")
@FullscreenPanelsCompatibility
@Version("0.0.1")
class FullscreenPanelsRemoverPatch : BytecodePatch(
    listOf(
        FullscreenViewAdderFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = FullscreenViewAdderFingerprint.result
            ?: return PatchResultError("Fingerprint not resolved!")

        val method = result.mutableMethod
        val implementation = method.implementation
            ?: return PatchResultError("Implementation not found!")

        val visibilityCallIndex = implementation.instructions.withIndex()
            .first { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.name == ("setVisibility") }.index

        method.addInstructions(
            visibilityCallIndex - 1, """
            invoke-static { }, Lapp/revanced/integrations/patches/FullscreenPanelsRemoverPatch;->getFullsceenPanelsVisibility()I
            move-result p1
        """
        )

        return PatchResultSuccess()
    }
}
