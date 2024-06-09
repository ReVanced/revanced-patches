package app.revanced.patches.reddit.ad.banner

import app.revanced.patcher.patch.resourcePatch

@Suppress("unused")
val hideBannerPatch = resourcePatch(
    description = "Hides banner ads from comments on subreddits.",
) {
    execute { context ->
        val resourceFilePath = "res/layout/merge_listheader_link_detail.xml"

        context.document[resourceFilePath].use { document ->
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
