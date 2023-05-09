package app.revanced.patches.youtube.layout.hide.shorts.bytecode.patch

import app.revanced.extensions.injectHideViewCall
import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.layout.hide.shorts.annotations.HideShortsComponentsCompatibility
import app.revanced.patches.youtube.layout.hide.shorts.bytecode.fingerprints.CreateShortsButtonsFingerprint
import app.revanced.patches.youtube.layout.hide.shorts.bytecode.fingerprints.ReelConstructorFingerprint
import app.revanced.patches.youtube.layout.hide.shorts.resource.patch.HideShortsComponentsResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Patch
@DependsOn(
    [
        IntegrationsPatch::class,
        LithoFilterPatch::class,
        HideShortsComponentsResourcePatch::class,
        ResourceMappingPatch::class
    ]
)
@Name("hide-shorts-components")
@Description("Hides components from YouTube Shorts.")
@HideShortsComponentsCompatibility
@Version("0.0.1")
class HideShortsComponentsPatch : BytecodePatch(listOf(CreateShortsButtonsFingerprint, ReelConstructorFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        // Hide the Shorts shelf.
        ReelConstructorFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex + 2
                val viewRegister = instruction<TwoRegisterInstruction>(insertIndex).registerA

                injectHideViewCall(
                    insertIndex,
                    viewRegister,
                    CLASS_DESCRIPTOR,
                    "hideShortsShelf"
                )
            }
        } ?: return ReelConstructorFingerprint.toErrorResult()

        // Some Shorts buttons are views, hide them by setting their visibility to GONE.
        CreateShortsButtonsFingerprint.result?.let {
            ShortsButtons.values().forEach { button -> button.injectHideCall(it.mutableMethod) }
        } ?: return CreateShortsButtonsFingerprint.toErrorResult()
        return PatchResultSuccess()
    }

    private companion object {
        private const val CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/components/ShortsFilter;"

        private enum class ShortsButtons(private val resourceName: String, private val methodName: String) {
            COMMENTS("reel_dyn_comment", "hideShortsCommentsButton"),
            REMIX("reel_dyn_remix", "hideShortsRemixButton"),
            SHARE("reel_dyn_share", "hideShortsShareButton");

            fun injectHideCall(method: MutableMethod) {
                val resourceId = ResourceMappingPatch.resourceMappings.single {
                    it.type == "id" && it.name == resourceName
                }.id

                // Get the index of the reference to the view's resource ID.
                val referencedIndex = method.implementation!!.instructions.indexOfFirst {
                    if (it.opcode != Opcode.CONST) return@indexOfFirst false

                    val literal = (it as WideLiteralInstruction).wideLiteral

                    return@indexOfFirst resourceId == literal
                }

                val setIdIndex = referencedIndex + 1
                val viewRegister = method.instruction<FiveRegisterInstruction>(setIdIndex).registerC
                method.injectHideViewCall(setIdIndex, viewRegister, CLASS_DESCRIPTOR, methodName)
            }
        }
    }
}
