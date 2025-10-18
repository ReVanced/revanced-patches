@file:Suppress("ktlint:standard:property-naming")

package app.revanced.patches.music.playservice

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findPlayStoreServicesVersion
import kotlin.properties.Delegates

// Use notNull delegate so an exception is thrown if these fields are accessed before they are set.

var is_7_16_or_greater: Boolean by Delegates.notNull()
    private set
var is_7_33_or_greater: Boolean by Delegates.notNull()
    private set
var is_8_05_or_greater: Boolean by Delegates.notNull()
    private set
var is_8_10_or_greater: Boolean by Delegates.notNull()
    private set
var is_8_11_or_greater: Boolean by Delegates.notNull()
    private set
var is_8_15_or_greater: Boolean by Delegates.notNull()
    private set

val versionCheckPatch = resourcePatch(
    description = "Uses the Play Store service version to find the major/minor version of the YouTube Music target app.",
) {
    execute {
        // The app version is missing from the decompiled manifest,
        // so instead use the Google Play services version and compare against specific releases.
        val playStoreServicesVersion = findPlayStoreServicesVersion()

        // All bug fix releases always seem to use the same play store version as the minor version.
        is_7_16_or_greater = 243499000 <= playStoreServicesVersion
        is_7_33_or_greater = 245199000 <= playStoreServicesVersion
        is_8_05_or_greater = 250599000 <= playStoreServicesVersion
        is_8_10_or_greater = 251099000 <= playStoreServicesVersion
        is_8_11_or_greater = 251199000 <= playStoreServicesVersion
        is_8_15_or_greater = 251530000 <= playStoreServicesVersion
    }
}
