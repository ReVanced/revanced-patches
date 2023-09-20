package app.revanced.patches.youtube.ad.general

import app.revanced.extensions.findMutableMethodOf
import app.revanced.extensions.injectHideViewCall
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.fix.verticalscroll.VerticalScrollPatch
import app.revanced.patches.youtube.ad.getpremium.HideGetPremiumPatch
import app.revanced.patches.youtube.misc.fix.backtoexitgesture.FixBackToExitGesturePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

@Patch(
    name = "Hide ads",
    description = "Removes general ads.",
    dependencies = [
        HideGetPremiumPatch::class,
        HideAdsResourcePatch::class,
        VerticalScrollPatch::class,
        FixBackToExitGesturePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch() {
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
