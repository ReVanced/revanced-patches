package app.revanced.patches.pixiv.ads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.pixiv.ads.fingerprints.ShouldShowAdsFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide ads",
    compatiblePackages = [CompatiblePackage("jp.pxv.android")],
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(setOf(ShouldShowAdsFingerprint)) {
    override fun execute(context: BytecodeContext) =
        ShouldShowAdsFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        ) ?: throw ShouldShowAdsFingerprint.exception
}
