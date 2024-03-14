package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.PivotBarConstructorFingerprint
import app.revanced.util.exception

@Patch(
    description = "Resolves necessary fingerprints.",
    dependencies = [ResourceMappingPatch::class]
)
internal object ResolvePivotBarFingerprintsPatch : BytecodePatch(
    setOf(PivotBarConstructorFingerprint)
) {
    internal var imageOnlyTabResourceId: Long = -1
    internal var actionBarSearchResultsViewMicId: Long = -1

    override fun execute(context: BytecodeContext) {
        // imageOnlyTabResourceId is used in InitializeButtonsFingerprint fingerprint
        imageOnlyTabResourceId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "image_only_tab"
        }.id

        actionBarSearchResultsViewMicId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "action_bar_search_results_view_mic"
        }.id

        PivotBarConstructorFingerprint.result?.let {
            // Resolve InitializeButtonsFingerprint on the class of the method
            // which PivotBarConstructorFingerprint resolved to
            if (!InitializeButtonsFingerprint.resolve(
                    context,
                    it.classDef
                )
            ) throw InitializeButtonsFingerprint.exception
        } ?: throw PivotBarConstructorFingerprint.exception
    }

}