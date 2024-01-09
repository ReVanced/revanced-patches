package app.revanced.patches.spotify.navbar

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.spotify.navbar.fingerprints.AddNavBarItemFingerprint

@Patch(
    name = "Hide premium navbar",
    description = "Removes the premium tab from the navbar.",
    dependencies = [PremiumNavbarTabResourcePatch::class],
    compatiblePackages = [CompatiblePackage("com.spotify.music")]
)
@Suppress("unused")
object PremiumNavbarTabPatch : BytecodePatch(
    setOf(AddNavBarItemFingerprint)
) {
    // If the navigation bar item is the premium tab, do not add it.
    override fun execute(context: BytecodeContext) = AddNavBarItemFingerprint.result?.mutableMethod?.addInstructions(
        0,
        """
            const v1, ${PremiumNavbarTabResourcePatch.premiumTabId}
            if-ne p5, v1, :continue
            return-void
            :continue
            nop
        """
    ) ?: throw AddNavBarItemFingerprint.exception
}
