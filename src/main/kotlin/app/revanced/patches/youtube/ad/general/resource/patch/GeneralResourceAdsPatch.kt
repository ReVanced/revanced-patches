package app.revanced.patches.youtube.ad.general.resource.patch

import app.revanced.extensions.doRecursively
import app.revanced.extensions.startsWithAny
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.ad.general.annotation.GeneralAdsCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import org.w3c.dom.Element

@Dependencies(dependencies = [FixLocaleConfigErrorPatch::class])
@Name("general-resource-ads")
@Description("Patch to remove general ads in resources.")
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralResourceAdsPatch : ResourcePatch() {
    // list of resource file names which need to be hidden
    private val resourceFileNames = arrayOf(
        "compact_promoted_video_item.xml",
        "inline_muted_metadata_swap.xml",
        "interstitial_promo_view.xml",
        "pip_ad_overlay.xml",
        "promoted_",
        "watch_metadata_companion_cards.xml",
        //"watch_while_activity.xml" // FIXME: find out why patching this resource fails
    )

    // the attributes to change the value of
    private val replacements = arrayOf(
        "height",
        "width",
        "marginTop",
    )

    override fun execute(data: ResourceData): PatchResult {
        data.forEach {
            if (!it.name.startsWithAny(*resourceFileNames)) return@forEach

            // for each file in the "layouts" directory replace all necessary attributes content
            data.xmlEditor[it.absolutePath].use { editor ->
                editor.file.doRecursively { node ->
                    replacements.forEach replacement@{ replacement ->
                        if (node !is Element) return@replacement

                        node.getAttributeNode("android:layout_$replacement")?.let { attribute ->
                            attribute.textContent = "1.0dip"
                        }
                    }
                }
            }
        }
        return PatchResultSuccess()
    }
}