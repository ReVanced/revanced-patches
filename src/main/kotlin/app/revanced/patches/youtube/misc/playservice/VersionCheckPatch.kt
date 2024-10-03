package app.revanced.patches.youtube.misc.playservice

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.util.findElementByAttributeValueOrThrow
import kotlin.properties.Delegates

/**
 * Uses the Play Store service version to find the major/minor version of the target app.
 * All bug fix releases always seem to use the same play store version.
 */
@Patch(dependencies = [ResourceMappingPatch::class])
internal object VersionCheckPatch : ResourcePatch() {

    private var playStoreServicesVersion by Delegates.notNull<Int>()

    var is_19_03_or_greater by Delegates.notNull<Boolean>()
    var is_19_04_or_greater by Delegates.notNull<Boolean>()
    var is_19_16_or_greater by Delegates.notNull<Boolean>()
    var is_19_17_or_greater by Delegates.notNull<Boolean>()
    var is_19_18_or_greater by Delegates.notNull<Boolean>()
    var is_19_23_or_greater by Delegates.notNull<Boolean>()
    var is_19_25_or_greater by Delegates.notNull<Boolean>()
    var is_19_26_or_greater by Delegates.notNull<Boolean>()
    var is_19_29_or_greater by Delegates.notNull<Boolean>()
    var is_19_32_or_greater by Delegates.notNull<Boolean>()
    var is_19_33_or_greater by Delegates.notNull<Boolean>()
    var is_19_36_or_greater by Delegates.notNull<Boolean>()

    override fun execute(context: ResourceContext) {
        /**
         * Used to check what version an app is.
         * Returns the Google Play services version,
         * since the decoded app manifest does not have the app version.
         */
        fun getPlayServicesVersion(context: ResourceContext): Int {
            // The app version is missing from the decompiled manifest,
            // so instead use the Google Play services version and compare against specific releases.
            context.document["res/values/integers.xml"].use { document ->
                return document.documentElement.childNodes.findElementByAttributeValueOrThrow(
                    "name",
                    "google_play_services_version"
                ).textContent.toInt()
            }
        }

        playStoreServicesVersion = getPlayServicesVersion(context)

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
        is_19_36_or_greater = 243705000 <= playStoreServicesVersion
    }
}
