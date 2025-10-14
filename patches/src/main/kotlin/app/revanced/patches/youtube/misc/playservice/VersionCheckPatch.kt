@file:Suppress("ktlint:standard:property-naming")

package app.revanced.patches.youtube.misc.playservice

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findPlayStoreServicesVersion
import kotlin.properties.Delegates

// Use notNull delegate so an exception is thrown if these fields are accessed before they are set.

@Deprecated("19.34.42 is the lowest supported version")
var is_19_03_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_04_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_16_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_17_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_18_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_23_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_25_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_26_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_29_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_32_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_33_or_greater : Boolean by Delegates.notNull()
    private set
@Deprecated("19.34.42 is the lowest supported version")
var is_19_34_or_greater : Boolean by Delegates.notNull()
    private set
var is_19_35_or_greater : Boolean by Delegates.notNull()
    private set
var is_19_36_or_greater : Boolean by Delegates.notNull()
    private set
var is_19_41_or_greater : Boolean by Delegates.notNull()
    private set
var is_19_43_or_greater : Boolean by Delegates.notNull()
    private set
var is_19_46_or_greater : Boolean by Delegates.notNull()
    private set
var is_19_47_or_greater : Boolean by Delegates.notNull()
    private set
var is_19_49_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_02_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_03_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_05_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_07_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_09_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_10_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_14_or_greater : Boolean by Delegates.notNull()
    private set
var is_20_15_or_greater : Boolean by Delegates.notNull()
    private set

val versionCheckPatch = resourcePatch(
    description = "Uses the Play Store service version to find the major/minor version of the YouTube target app.",
) {
    execute {
        // The app version is missing from the decompiled manifest,
        // so instead use the Google Play services version and compare against specific releases.
        val playStoreServicesVersion = findPlayStoreServicesVersion()

        // All bug fix releases always seem to use the same play store version as the minor version.
        is_19_03_or_greater = 240402000 <= playStoreServicesVersion
        is_19_04_or_greater = 240502000 <= playStoreServicesVersion
        is_19_16_or_greater = 241702000 <= playStoreServicesVersion
        is_19_17_or_greater = 241802000 <= playStoreServicesVersion
        is_19_18_or_greater = 241902000 <= playStoreServicesVersion
        is_19_23_or_greater = 242402000 <= playStoreServicesVersion
        is_19_25_or_greater = 242599000 <= playStoreServicesVersion
        is_19_26_or_greater = 242705000 <= playStoreServicesVersion
        is_19_29_or_greater = 243005000 <= playStoreServicesVersion
        is_19_32_or_greater = 243199000 <= playStoreServicesVersion
        is_19_33_or_greater = 243405000 <= playStoreServicesVersion
        is_19_34_or_greater = 243499000 <= playStoreServicesVersion
        is_19_35_or_greater = 243605000 <= playStoreServicesVersion
        is_19_36_or_greater = 243705000 <= playStoreServicesVersion
        is_19_41_or_greater = 244305000 <= playStoreServicesVersion
        is_19_43_or_greater = 244405000 <= playStoreServicesVersion
        is_19_46_or_greater = 244705000 <= playStoreServicesVersion
        is_19_47_or_greater = 244799000 <= playStoreServicesVersion
        is_19_49_or_greater = 245005000 <= playStoreServicesVersion
        is_20_02_or_greater = 250299000 <= playStoreServicesVersion
        is_20_03_or_greater = 250405000 <= playStoreServicesVersion
        is_20_05_or_greater = 250605000 <= playStoreServicesVersion
        is_20_07_or_greater = 250805000 <= playStoreServicesVersion
        is_20_09_or_greater = 251006000 <= playStoreServicesVersion
        is_20_10_or_greater = 251105000 <= playStoreServicesVersion
        is_20_14_or_greater = 251505000 <= playStoreServicesVersion
        is_20_15_or_greater = 251605000 <= playStoreServicesVersion
    }
}
