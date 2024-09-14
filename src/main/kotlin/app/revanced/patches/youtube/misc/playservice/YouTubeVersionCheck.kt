package app.revanced.patches.youtube.misc.playservice

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import org.w3c.dom.Element
import org.w3c.dom.Node
import kotlin.properties.Delegates

/**
 * Uses the Play Store service version to find the major/minor version of the target app.
 * All bug fix releases always seem to use the same play store version.
 */
@Patch(dependencies = [ResourceMappingPatch::class])
internal object YouTubeVersionCheck : ResourcePatch() {

    private var playStoreServicesVersion by Delegates.notNull<Int>()

    var is_19_15_or_greater by Delegates.notNull<Boolean>()
    var is_19_16_or_greater by Delegates.notNull<Boolean>()
    var is_19_17_or_greater by Delegates.notNull<Boolean>()
    var is_19_18_or_greater by Delegates.notNull<Boolean>()
    var is_19_19_or_greater by Delegates.notNull<Boolean>()
    var is_19_23_or_greater by Delegates.notNull<Boolean>()
    var is_19_24_or_greater by Delegates.notNull<Boolean>()
    var is_19_25_or_greater by Delegates.notNull<Boolean>()
    var is_19_26_or_greater by Delegates.notNull<Boolean>()
    var is_19_29_or_greater by Delegates.notNull<Boolean>()
    var is_19_32_or_greater by Delegates.notNull<Boolean>()
    var is_19_33_or_greater by Delegates.notNull<Boolean>()
    var is_19_35_or_greater by Delegates.notNull<Boolean>()
    var is_19_36_or_greater by Delegates.notNull<Boolean>()

    override fun execute(context: ResourceContext) {
        playStoreServicesVersion = findPlayServicesVersion(context)

        is_19_15_or_greater = 241602000 <= playStoreServicesVersion
        is_19_16_or_greater = 241702000 <= playStoreServicesVersion
        is_19_17_or_greater = 241802000 <= playStoreServicesVersion
        is_19_18_or_greater = 241902000 <= playStoreServicesVersion
        is_19_19_or_greater = 241999000 <= playStoreServicesVersion
        is_19_23_or_greater = 242402000 <= playStoreServicesVersion
        is_19_24_or_greater = 242505000 <= playStoreServicesVersion
        is_19_25_or_greater = 242599000 <= playStoreServicesVersion
        is_19_26_or_greater = 242705000 <= playStoreServicesVersion
        is_19_29_or_greater = 243005000 <= playStoreServicesVersion
        is_19_32_or_greater = 243199000 <= playStoreServicesVersion
        is_19_33_or_greater = 243405000 <= playStoreServicesVersion
        is_19_35_or_greater = 243605000 <= playStoreServicesVersion
        is_19_36_or_greater = 243705000 <= playStoreServicesVersion
    }

    /**
     * Used to check what version an app is.
     * Returns the Google Play services version,
     * since the decoded app manifest does not have the app version.
     */
    private fun findPlayServicesVersion(context: ResourceContext): Int {
        // The app version is missing from the decompiled manifest,
        // so instead use the Google Play services version and compare against specific releases.
        context.document["res/values/integers.xml"].use { document ->
            val nodeList = document.documentElement.childNodes
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element
                    if (element.getAttribute("name") == "google_play_services_version") {
                        return element.textContent.toInt()
                    }
                }
            }
        }

        throw PatchException("integers.xml does not contain a Google Play services version")
    }
}
