@file:Suppress("ktlint:standard:property-naming")

package app.revanced.patches.music.playservice

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findPlayStoreServicesVersion

var is_7_33_or_greater = false
    private set
var is_8_10_or_greater = false
    private set
var is_8_11_or_greater = false
    private set
var is_8_15_or_greater = false
    private set

val versionCheckPatch = resourcePatch(
    description = "Uses the Play Store service version to find the major/minor version of the YouTube Music target app.",
) {
    execute {
        // The app version is missing from the decompiled manifest,
        // so instead use the Google Play services version and compare against specific releases.
        val playStoreServicesVersion = findPlayStoreServicesVersion()

        // All bug fix releases always seem to use the same play store version as the minor version.
        is_7_33_or_greater = 245199000 <= playStoreServicesVersion
        is_8_10_or_greater = 244799000 <= playStoreServicesVersion
        is_8_11_or_greater = 251199000 <= playStoreServicesVersion
        is_8_15_or_greater = 251530000 <= playStoreServicesVersion
    }
}
