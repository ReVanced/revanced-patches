package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.PivotBarConstructorFingerprint

@Patch(
    description = "Resolves necessary fingerprints.",
    dependencies = [ResourceMappingPatch::class]
)
internal object ResolvePivotBarFingerprintsPatch : BytecodePatch(
    setOf(PivotBarConstructorFingerprint)
) {
    internal var imageOnlyTabResourceId: Long = -1

    override fun execute(context: BytecodeContext) {
        // imageOnlyTabResourceId is used in InitializeButtonsFingerprint fingerprint
        ResourceMappingPatch.resourceMappings.find { it.type == "layout" && it.name == "image_only_tab" }
            ?.let { imageOnlyTabResourceId = it.id } ?: throw PatchException("Failed to find resource")

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