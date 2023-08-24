package app.revanced.patches.youtube.layout.buttons.navigation.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.PivotBarConstructorFingerprint

@DependsOn([ResourceMappingPatch::class])
@Description("Resolves necessary fingerprints.")
class ResolvePivotBarFingerprintsPatch : BytecodePatch(
    listOf(PivotBarConstructorFingerprint)
) {
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

    internal companion object {
        var imageOnlyTabResourceId: Long = -1
    }
}