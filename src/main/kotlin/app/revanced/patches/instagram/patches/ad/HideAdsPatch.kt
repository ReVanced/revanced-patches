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
    description = "Hides ads in stories, discover, profile, etc. " +
        "An ad can still appear once when refreshing the home feed.",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(
    setOf(AdInjectorFingerprint),
) {
    override fun execute(context: BytecodeContext) =
        AdInjectorFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        ) ?: throw AdInjectorFingerprint.exception
}
