package app.revanced.patches.twitter.layout.viewcount


import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.layout.viewcount.fingerprints.ViewCountsEnabledFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide view count",
    description = "Hides the view count of Posts.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")],
    use = false
)
@Suppress("unused")
object HideViewCountPatch : BytecodePatch(
    setOf(ViewCountsEnabledFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        ViewCountsEnabledFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        ) ?: throw ViewCountsEnabledFingerprint.exception
}
