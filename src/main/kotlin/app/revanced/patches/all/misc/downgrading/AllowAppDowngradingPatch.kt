package app.revanced.patches.all.misc.downgrading


import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import org.w3c.dom.Element


@Patch(
    name = "Allow app downgrading",
    description = "Allow app downgrading by setting the app version code to 1",
    use = false
)
@Suppress("unused")
object AllowAppDowngradingPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.document["AndroidManifest.xml"].use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            manifest.setAttribute("android:versionCode", "1")
        }
    }
}

