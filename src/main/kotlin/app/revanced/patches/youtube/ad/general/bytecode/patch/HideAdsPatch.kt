package app.revanced.patches.youtube.ad.general.bytecode.patch

import app.revanced.extensions.findMutableMethodOf
import app.revanced.extensions.injectHideViewCall
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.misc.fix.verticalscroll.patch.VerticalScrollPatch
import app.revanced.patches.youtube.ad.general.annotation.HideAdsCompatibility
import app.revanced.patches.youtube.ad.general.resource.patch.HideAdsResourcePatch
import app.revanced.patches.youtube.ad.getpremium.bytecode.patch.HideGetPremiumPatch
import app.revanced.patches.youtube.misc.fix.backtoexitgesture.patch.FixBackToExitGesturePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c


@Patch
@DependsOn(
    [
        HideGetPremiumPatch::class,
        HideAdsResourcePatch::class,
        VerticalScrollPatch::class,
        FixBackToExitGesturePatch::class
    ]
)
@Name("Hide ads")
@Description("Removes general ads.")
@HideAdsCompatibility
class HideAdsPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        context.classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                with(method.implementation) {
                    this?.instructions?.forEachIndexed { index, instruction ->
                        if (instruction.opcode != Opcode.CONST)
                            return@forEachIndexed
                        // Instruction to store the id adAttribution into a register
                        if ((instruction as Instruction31i).wideLiteral != HideAdsResourcePatch.adAttributionId)
                            return@forEachIndexed

                        val insertIndex = index + 1

                        // Call to get the view with the id adAttribution
                        with(instructions.elementAt(insertIndex)) {
                            if (opcode != Opcode.INVOKE_VIRTUAL)
                                return@forEachIndexed

                            // Hide the view
                            val viewRegister = (this as Instruction35c).registerC
                            context.proxy(classDef)
                                .mutableClass
                                .findMutableMethodOf(method)
                                .injectHideViewCall(
                                    insertIndex,
                                    viewRegister,
                                    "Lapp/revanced/integrations/patches/components/AdsFilter;",
                                    "hideAdAttributionView"
                                )
                        }
                    }
                }
            }
        }
    }
}
