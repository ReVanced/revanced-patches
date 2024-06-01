package app.revanced.patches.reddit.ad.banner

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch

// Note that for now, this patch and anything using it will only work on
// Reddit 2024.17.0 or older. Newer versions will crash during patching.
// See https://github.com/ReVanced/revanced-patches/issues/3099
@Patch(description = "Hides banner ads from comments on subreddits.")
object HideBannerPatch : ResourcePatch() {
    private const val RESOURCE_FILE_PATH = "res/layout/merge_listheader_link_detail.xml"

    override fun execute(context: ResourceContext) {
        context.xmlEditor[RESOURCE_FILE_PATH].use { editor ->
            val document = editor.file

            document.getElementsByTagName("merge").item(0).childNodes.apply {
                val attributes = arrayOf("height", "width")

                for (i in 1 until length) {
                    val view = item(i)
                    if (
                        view.hasAttributes() &&
                        view.attributes.getNamedItem("android:id").nodeValue.endsWith("ad_view_stub")
                    ) {
                        attributes.forEach { attribute ->
                            view.attributes.getNamedItem("android:layout_$attribute").nodeValue = "0.0dip"
                        }

                        break
                    }
                }
            }
        }
    }
}
