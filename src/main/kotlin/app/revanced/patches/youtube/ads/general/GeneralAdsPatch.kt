package app.revanced.patches.youtube.ads.general

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.ads.getpremium.HideGetPremiumPatch
import app.revanced.patches.youtube.utils.fix.doublebacktoclose.DoubleBackToClosePatch
import app.revanced.patches.youtube.utils.fix.swiperefresh.SwipeRefreshPatch
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.copyXmlNode
import app.revanced.util.doRecursively
import app.revanced.util.startsWithAny
import org.w3c.dom.Element

@Patch(
    name = "Hide general ads",
    description = "Adds options to hide general ads.",
    dependencies = [
        DoubleBackToClosePatch::class,
        GeneralAdsBytecodePatch::class,
        HideGetPremiumPatch::class,
        LithoFilterPatch::class,
        SettingsPatch::class,
        SwipeRefreshPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object GeneralAdsPatch : ResourcePatch() {
    private val resourceFileNames = arrayOf(
        "promoted_",
        "promotion_",
        "compact_premium_",
        "compact_promoted_",
        "simple_text_section",
    )

    private val replacements = arrayOf(
        "height",
        "width",
        "marginTop"
    )

    private val additionalReplacements = arrayOf(
        "Bottom",
        "End",
        "Start",
        "Top"
    )

    override fun execute(context: ResourceContext) {
        LithoFilterPatch.addFilter("$COMPONENTS_PATH/AdsFilter;")

        context.forEach {

            if (!it.name.startsWithAny(*resourceFileNames)) return@forEach

            // for each file in the "layouts" directory replace all necessary attributes content
            context.xmlEditor[it.absolutePath].use { editor ->
                editor.file.doRecursively {
                    replacements.forEach replacement@{ replacement ->
                        if (it !is Element) return@replacement

                        it.getAttributeNode("android:layout_$replacement")?.let { attribute ->
                            attribute.textContent = "0.0dip"
                        }
                    }
                }
            }
        }

        context.xmlEditor["res/layout/simple_text_section.xml"].use { editor ->
            editor.file.doRecursively {
                additionalReplacements.forEach replacement@{ replacement ->
                    if (it !is Element) return@replacement

                    it.getAttributeNode("android:padding_$replacement")?.let { attribute ->
                        attribute.textContent = "0.0dip"
                    }
                }
            }
        }

        /**
         * Copy arrays
         */
        context.copyXmlNode("youtube/doubleback/host", "values/arrays.xml", "resources")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: ADS_SETTINGS",
                "SETTINGS: HIDE_GENERAL_ADS",

                "SETTINGS: DOUBLE_BACK_TIMEOUT"
            )
        )

        SettingsPatch.updatePatchStatus("Hide general ads")

    }
}