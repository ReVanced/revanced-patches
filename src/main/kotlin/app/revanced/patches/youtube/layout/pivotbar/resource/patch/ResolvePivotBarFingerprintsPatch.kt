package app.revanced.patches.youtube.layout.pivotbar.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.layout.pivotbar.annotations.PivotBarCompatibility
import app.revanced.patches.youtube.layout.pivotbar.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.layout.pivotbar.fingerprints.PivotBarConstructorFingerprint
import app.revanced.patches.youtube.layout.pivotbar.utils.InjectionUtils.toErrorResult

@DependsOn([ResourceMappingPatch::class])
@PivotBarCompatibility
@Description("Resolves necessary fingerprints.")
@Version("0.0.1")
class ResolvePivotBarFingerprintsPatch : BytecodePatch(
    listOf(PivotBarConstructorFingerprint)
) {
    internal companion object {
        var imageOnlyTabResourceId: Long = -1
    }

    override fun execute(context: BytecodeContext): PatchResult {
        // imageOnlyTabResourceId is used in InitializeButtonsFingerprint fingerprint
        ResourceMappingPatch.resourceMappings.find { it.type == "layout" && it.name == "image_only_tab" }
            ?.let { imageOnlyTabResourceId = it.id } ?: return PatchResultError("Failed to find resource")

        PivotBarConstructorFingerprint.result?.let {
            // Resolve InitializeButtonsFingerprint on the class of the method
            // which PivotBarConstructorFingerprint resolved to
            if (!InitializeButtonsFingerprint.resolve(
                    context,
                    it.classDef
                )
            ) return InitializeButtonsFingerprint.toErrorResult()
        } ?: return PivotBarConstructorFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}