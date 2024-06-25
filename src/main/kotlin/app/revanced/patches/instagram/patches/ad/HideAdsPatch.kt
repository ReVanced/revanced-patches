package app.revanced.patches.instagram.patches.ad

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.patches.ad.fingerprints.AdInjectorFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide ads",
    description = "An ad can still appear when you refresh your home feed",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(
    setOf(
        AdInjectorFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        AdInjectorFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v5, 0x0
                return v5
            """
        ) ?: throw AdInjectorFingerprint.exception
    }
}
