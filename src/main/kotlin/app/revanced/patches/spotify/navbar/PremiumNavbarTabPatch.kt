package app.revanced.patches.spotify.navbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.spotify.navbar.fingerprints.AddPremiumNavbarTabFingerprint
import app.revanced.patches.spotify.navbar.fingerprints.AddPremiumNavbarTabParentFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction

@Patch(
    name = "Hide premium navbar",
    description = "Removes the premium tab from the navbar.",
    dependencies = [ResourceMappingPatch::class],
    compatiblePackages = [CompatiblePackage("com.spotify.music")]
)
@Suppress("unused")
object PremiumNavbarTabPatch : BytecodePatch(setOf(AddPremiumNavbarTabParentFingerprint)) {
    override fun execute(context: BytecodeContext) {
        val parentResult = AddPremiumNavbarTabParentFingerprint.result!!
        AddPremiumNavbarTabFingerprint.resolve(context, parentResult.classDef)

        val result = AddPremiumNavbarTabFingerprint.result!!

        val method = result.mutableMethod
        val methodInstructions = method.implementation!!.instructions
        val lastInstructionIdx = methodInstructions.size - 1

        val premiumTabId =
            ResourceMappingPatch.resourceMappings.single { it.type == "id" && it.name == "premium_tab" }.id

        var removeAmount = 2
        // 2nd const remove method
        for ((i, instruction) in methodInstructions.asReversed().withIndex()) {
            if (instruction.opcode.ordinal != Opcode.CONST.ordinal) continue
            if ((instruction as WideLiteralInstruction).wideLiteral != premiumTabId) continue

            val findThreshold = 10
            val constIndex = lastInstructionIdx - i
            val invokeInstruction = methodInstructions.subList(constIndex, constIndex + findThreshold).first {
                it.opcode.ordinal == Opcode.INVOKE_VIRTUAL_RANGE.ordinal
            }
            method.removeInstruction(methodInstructions.indexOf(invokeInstruction))

            if (--removeAmount == 0) break
        }
    }
}