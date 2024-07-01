package app.revanced.patches.stocard.layout

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.childElementsSequence
import app.revanced.util.getNode

@Patch(
    name = "Hide offers tab",
    compatiblePackages = [CompatiblePackage("de.stocard.stocard")],
)
@Suppress("unused")
object HideOffersTabPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.document["res/menu/bottom_navigation_menu.xml"].use { document ->
            document.getNode("menu").apply {
                removeChild(
                    childElementsSequence().first {
                        it.attributes.getNamedItem("android:id")?.nodeValue?.contains("offer") ?: false
                    },
                )
            }
        }
    }
}
