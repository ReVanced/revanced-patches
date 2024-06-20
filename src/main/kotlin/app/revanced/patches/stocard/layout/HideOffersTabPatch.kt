package app.revanced.patches.stocard.layout

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.childElementsSequence

@Patch(name = "Hide offers tab", compatiblePackages = [CompatiblePackage("de.stocard.stocard")])
object HideOffersTabPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.document["res/menu/bottom_navigation_menu.xml"].use { document ->
            val menu = document.getElementsByTagName("menu").item(0)
            menu.removeChild(menu.childElementsSequence()
                .first { it.attributes.getNamedItem("android:id")?.nodeValue?.contains("offer") ?: false })
        }
    }
}