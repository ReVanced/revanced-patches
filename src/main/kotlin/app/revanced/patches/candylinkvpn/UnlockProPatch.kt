package app.revanced.patches.candylinkvpn

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.candylinkvpn.fingerprints.IsPremiumPurchasedFingerprint
import app.revanced.util.exception

@Patch(
    compatiblePackages = [CompatiblePackage("com.candylink.openvpn")],
)
@Deprecated(
    "This patch does not work anymore and will be removed in the future, " +
        "because the servers now check the purchase status.",
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(
    setOf(IsPremiumPurchasedFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        IsPremiumPurchasedFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
               const/4 v0, 0x1
               return v0
            """,
        ) ?: throw IsPremiumPurchasedFingerprint.exception
    }
}
