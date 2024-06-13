package app.revanced.patches.all.misc.versioncode


import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.intPatchOption
import org.w3c.dom.Element


@Patch(
    name = "Change version code",
    description = "Sets the version code to the max value (allowed by Google Play) by default, which enables app downgrading and hides available updates from app stores like Google Play",
    use = false
)
@Suppress("unused")
object ChangeVersionCode : ResourcePatch() {
    private val versionCodeOption by intPatchOption(
        key = "versionCode",
        // max allowed by Google Play
        default = 2_100_000_000,
        values = mapOf(
            "Minimum" to 1, "Maximum" to 2_100_000_000
        ),
        title = "Version code",
        description = "The version code to use",
        required = true,
    ) {
        it != null && it >= 1 && it <= 2_100_000_000
    }

    override fun execute(context: ResourceContext) {
        context.document["AndroidManifest.xml"].use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            manifest.setAttribute("android:versionCode", "$versionCodeOption")
        }
    }
}

