package app.revanced.patches.youtube.misc.bottomsheet.hook.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch

@DependsOn([ResourceMappingPatch::class])
class BottomSheetHookResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        bottomSheetMargins =  ResourceMappingPatch.resourceMappings.find { it.name == "bottom_sheet_margins" }?.id
            ?: throw PatchException("Could not find resource")
    }

    internal companion object {
        var bottomSheetMargins = -1L
    }
}