package app.revanced.patches.youtube.misc.bottomsheet.hook.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch

@DependsOn([ResourceMappingPatch::class])
class BottomSheetHookResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        bottomSheetMargins =  ResourceMappingPatch.resourceMappings.find { it.name == "bottomSheetMargins" }?.id
            ?: return PatchResultError("Could not find resource")

        return PatchResultSuccess()
    }

    internal companion object {
        var bottomSheetMargins = -1L
    }
}