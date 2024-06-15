package app.revanced.patches.all.misc.versioncode

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.intPatchOption
import app.revanced.util.getNode
import org.w3c.dom.Element

@Patch(
    name = "Change version code",
    description = "Changes the version code of the app. By default the highest version code is set. " +
        "This allows older versions of an app to be installed " +
        "if their version code is set to the same or a higher value and can stop app stores to update the app.",
    use = false,
)
@Suppress("unused")
object ChangeVersionCodePatch : ResourcePatch() {
    private val versionCode by intPatchOption(
        key = "versionCode",
        default = Int.MAX_VALUE,
        values = mapOf(
            "Lowest" to 1,
            "Highest" to Int.MAX_VALUE,
        ),
        title = "Version code",
        description = "The version code to use",
        required = true,
    ) {
        it!! >= 1
    }

    override fun execute(context: ResourceContext) {
        context.document["AndroidManifest.xml"].use { document ->
            val manifestElement = document.getNode("manifest") as Element
            manifestElement.setAttribute("android:versionCode", "$versionCode")
        }
    }
}
