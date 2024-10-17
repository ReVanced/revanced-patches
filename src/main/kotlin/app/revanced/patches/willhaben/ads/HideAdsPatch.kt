package app.revanced.patches.willhaben.ads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.willhaben.ads.fingerprints.AdResolverFingerprint
import app.revanced.patches.willhaben.ads.fingerprints.WHAdViewInjectorFingerprint
import app.revanced.util.returnEarly

@Patch(
    name = "Hide ads",
    description = "Hides all in-app ads.",
    compatiblePackages = [CompatiblePackage("at.willhaben")]
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(
    fingerprints = setOf(
        AdResolverFingerprint,
        WHAdViewInjectorFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        arrayOf(AdResolverFingerprint, WHAdViewInjectorFingerprint).forEach {
            it.returnEarly()
        }
    }
}
