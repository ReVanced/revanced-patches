package app.revanced.patches.youtube.ad.home.patch

import app.revanced.extensions.injectHideCall
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.data.implementation.toMethodWalker
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.ad.home.annotation.PromotionsCompatibility
import app.revanced.patches.youtube.ad.home.signatures.PromotedDiscoveryActionParentSignature
import app.revanced.patches.youtube.ad.home.signatures.PromotedDiscoveryAppParentSignature
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction11x

@Patch
@Name("home-promo-ads")
@Description("Patch to remove promoted ads in YouTube.")
@PromotionsCompatibility
@Version("0.0.1")
class PromotionsPatch : BytecodePatch(
    listOf(
        PromotedDiscoveryAppParentSignature, PromotedDiscoveryActionParentSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        for (signature in signatures) {
            val result = signature.result!!

            val requiredMethod =
                result.findParentMethod(@Name("promotion-ads-signature") @MatchingMethod(name = "d") @DirectPatternScanMethod @PromotionsCompatibility @Version(
                    "0.0.1"
                ) object : MethodSignature(
                    "V", AccessFlags.PRIVATE or AccessFlags.FINAL, listOf("Z", "Z"), null
                ) {}) ?: return PatchResultError("Required parent method could not be found.")

            val toBePatchedInvokeOffset =
                requiredMethod.immutableMethod.implementation!!.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_DIRECT }
            val toBePatchedMethod =
                data.toMethodWalker(requiredMethod.immutableMethod).walk(toBePatchedInvokeOffset, true)
                    .getMethod() as MutableMethod

            val implementation = toBePatchedMethod.implementation!!
            val invokeVirtualOffset = implementation.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_VIRTUAL }

            val moveResultInstruction = implementation.instructions[invokeVirtualOffset + 1]
            if (moveResultInstruction.opcode != Opcode.MOVE_RESULT_OBJECT) return PatchResultError("The toBePatchedInvokeOffset offset was wrong in ${(this::class.annotations.find { it is Name } as Name).name}")

            val register = (moveResultInstruction as Instruction11x).registerA
            implementation.injectHideCall(invokeVirtualOffset + 2, register)
        }

        return PatchResultSuccess()
    }
}