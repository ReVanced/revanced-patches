package app.revanced.patches.googlephotos.features

import app.revanced.patches.all.misc.build.BuildInfo
import app.revanced.patches.all.misc.build.baseSpoofBuildInfoPatch

// Spoof build info to Google Pixel XL.
@Suppress("unused")
val spoofBuildInfoPatch = baseSpoofBuildInfoPatch {
    BuildInfo(
        brand = "google",
        manufacturer = "Google",
        device = "marlin",
        product = "marlin",
        model = "Pixel XL",
        fingerprint = "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys",
    )
}
