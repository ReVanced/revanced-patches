package app.revanced.patches.spotify.navbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.getResourceId
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch

internal var premiumTabId = -1L
    private set

private val premiumNavbarTabResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        premiumTabId = getResourceId("id", "premium_tab")
    }
}

@Suppress("unused")
val premiumNavbarTabPatch = bytecodePatch(
    name = "Premium navbar tab",
    description = "Hides the premium tab from the navigation bar.",
) {
    dependsOn(premiumNavbarTabResourcePatch)

    compatibleWith("com.spotify.music")

    // If the navigation bar item is the premium tab, do not add it.
    execute {
        addNavBarItemFingerprint.method.addInstructions(
            0,
            """
                const v1, $premiumTabId
                if-ne p5, v1, :continue
                return-void
                :continue
                nop
            """,
        )
    }
}
