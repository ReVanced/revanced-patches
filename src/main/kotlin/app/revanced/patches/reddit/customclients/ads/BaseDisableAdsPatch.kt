package app.revanced.patches.reddit.customclients.ads

import app.revanced.patcher.patch.Package
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.ads.fingerprints.isAdsEnabledFingerprint
import app.revanced.util.returnEarly

fun baseDisableAdsPatch(
    dependencies: Patch<*>,
    compatiblePackages: Package
) = bytecodePatch(
    name = "Disable ads"
) {
    dependsOn(dependencies)

    compatibleWith(compatiblePackages)

    isAdsEnabledFingerprint()

    execute {
        listOf(isAdsEnabledFingerprint).returnEarly()
    }
}