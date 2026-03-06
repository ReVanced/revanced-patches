@file:Suppress("ktlint:standard:property-naming")

package app.revanced.patches.youtube.misc.playservice

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findPlayStoreServicesVersion
import kotlin.properties.Delegates

// Use notNull delegate so an exception is thrown if these fields are accessed before they are set.

var is_20_15_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_19_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_20_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_21_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_22_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_26_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_28_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_29_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_30_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_31_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_34_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_37_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_39_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_40_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_41_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_45_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_46_or_greater: Boolean by Delegates.notNull()
    private set
var is_20_49_or_greater: Boolean by Delegates.notNull()
    private set
var is_21_02_or_greater: Boolean by Delegates.notNull()
    private set
var is_21_03_or_greater: Boolean by Delegates.notNull()
    private set
var is_21_05_or_greater : Boolean by Delegates.notNull()
    private set
var is_21_06_or_greater : Boolean by Delegates.notNull()
    private set
var is_21_07_or_greater : Boolean by Delegates.notNull()
    private set
var is_21_08_or_greater : Boolean by Delegates.notNull()
    private set

val versionCheckPatch = resourcePatch(
    description = "Uses the Play Store service version to find the major/minor version of the YouTube target app.",
) {
    apply {
        // The app version is missing from the decompiled manifest,
        // so instead use the Google Play services version and compare against specific releases.
        val playStoreServicesVersion = findPlayStoreServicesVersion()

        // All bug fix releases always seem to use the same play store version as the minor version.
        is_20_15_or_greater = 251605000 <= playStoreServicesVersion
        is_20_19_or_greater = 252005000 <= playStoreServicesVersion
        is_20_20_or_greater = 252105000 <= playStoreServicesVersion
        is_20_21_or_greater = 252205000 <= playStoreServicesVersion
        is_20_22_or_greater = 252305000 <= playStoreServicesVersion
        is_20_26_or_greater = 252705000 <= playStoreServicesVersion
        is_20_28_or_greater = 252905000 <= playStoreServicesVersion
        is_20_29_or_greater = 253005000 <= playStoreServicesVersion
        is_20_30_or_greater = 253105000 <= playStoreServicesVersion
        is_20_31_or_greater = 253205000 <= playStoreServicesVersion
        is_20_34_or_greater = 253505000 <= playStoreServicesVersion
        is_20_37_or_greater = 253805000 <= playStoreServicesVersion
        is_20_39_or_greater = 253980000 <= playStoreServicesVersion
        is_20_40_or_greater = 254105000 <= playStoreServicesVersion
        is_20_41_or_greater = 254205000 <= playStoreServicesVersion
        is_20_45_or_greater = 254605000 <= playStoreServicesVersion
        is_20_46_or_greater = 254705000 <= playStoreServicesVersion
        is_20_49_or_greater = 255005000 <= playStoreServicesVersion
        is_21_02_or_greater = 260305000 <= playStoreServicesVersion
        is_21_03_or_greater = 260405000 <= playStoreServicesVersion
        is_21_05_or_greater = 260605000 <= playStoreServicesVersion
        is_21_06_or_greater = 260705000 <= playStoreServicesVersion
        is_21_07_or_greater = 260805000 <= playStoreServicesVersion
        is_21_08_or_greater = 260905000 <= playStoreServicesVersion
    }
}
