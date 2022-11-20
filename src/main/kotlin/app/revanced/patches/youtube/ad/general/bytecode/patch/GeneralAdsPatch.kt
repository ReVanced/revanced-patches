package app.revanced.patches.youtube.ad.general.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.softCompareTo
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patches.youtube.ad.general.annotation.GeneralAdsCompatibility
import app.revanced.patches.youtube.ad.general.resource.patch.GeneralAdsResourcePatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.*
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.formats.Instruction31i
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference
import java.util.*


@Patch
@DependsOn([GeneralAdsResourcePatch::class])
@Name("general-ads")
@Description("Removes general ads.")
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralAdsPatch : BytecodePatch() {
    internal companion object {
        private fun MutableClass.findMutableMethodOf(
            method: Method
        ) = this.methods.first {
            it.softCompareTo(
                ImmutableMethodReference(
                    method.definingClass, method.name, method.parameters, method.returnType
                )
            )
        }
    }

    override fun execute(context: BytecodeContext): PatchResult {
        context.classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                with(method.implementation) {
                    this?.instructions?.forEachIndexed { index, instruction ->
                        if (instruction.opcode != org.jf.dexlib2.Opcode.CONST)
                            return@forEachIndexed
                        // Instruction to store the id adAttribution into a register
                        if ((instruction as Instruction31i).wideLiteral != GeneralAdsResourcePatch.adAttributionId)
                            return@forEachIndexed

                        val insertIndex = index + 1

                        // Call to get the view with the id adAttribution
                        with(instructions.elementAt(insertIndex)) {
                            if (opcode != org.jf.dexlib2.Opcode.INVOKE_VIRTUAL)
                                return@forEachIndexed

                            // Hide the view
                            val viewRegister = (this as Instruction35c).registerC
                            context.proxy(classDef).mutableClass.findMutableMethodOf(method).addInstruction(
                                insertIndex,
                                "invoke-static { v$viewRegister }, " +
                                        "Lapp/revanced/integrations/patches/GeneralAdsPatch;" +
                                        "->" +
                                        "hideAdAttributionView(Landroid/view/View;)V"
                            )
                        }
                    }
                }
            }
        }

        return PatchResultSuccess()
    }

}
