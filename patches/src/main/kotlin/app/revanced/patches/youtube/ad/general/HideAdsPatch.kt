package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.fix.verticalscroll.verticalScrollPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.ad.getpremium.hideGetPremiumPatch
import app.revanced.patches.youtube.misc.fix.backtoexitgesture.fixBackToExitGesturePatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.findMutableMethodOf
import app.revanced.util.injectHideViewCall
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

internal var adAttributionId = -1L
    private set

private val hideAdsResourcePatch = resourcePatch {
    dependsOn(
        lithoFilterPatch,
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "ad.general.hideAdsResourcePatch")

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_hide_general_ads"),
            SwitchPreference("revanced_hide_fullscreen_ads"),
            SwitchPreference("revanced_hide_buttoned_ads"),
            SwitchPreference("revanced_hide_paid_promotion_label"),
            SwitchPreference("revanced_hide_player_store_shelf"),
            SwitchPreference("revanced_hide_self_sponsor_ads"),
            SwitchPreference("revanced_hide_products_banner"),
            SwitchPreference("revanced_hide_shopping_links"),
            SwitchPreference("revanced_hide_visit_store_button"),
            SwitchPreference("revanced_hide_web_search_results"),
            SwitchPreference("revanced_hide_merchandise_banners"),
        )

        addLithoFilter("Lapp/revanced/extension/youtube/patches/components/AdsFilter;")

        adAttributionId = resourceMappings["id", "ad_attribution"]
    }
}

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Adds options to remove general ads.",
) {
    dependsOn(
        hideGetPremiumPatch,
        hideAdsResourcePatch,
        verticalScrollPatch,
        fixBackToExitGesturePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                with(method.implementation) {
                    this?.instructions?.forEachIndexed { index, instruction ->
                        if (instruction.opcode != Opcode.CONST) {
                            return@forEachIndexed
                        }
                        // Instruction to store the id adAttribution into a register
                        if ((instruction as Instruction31i).wideLiteral != adAttributionId) {
                            return@forEachIndexed
                        }

                        val insertIndex = index + 1

                        // Call to get the view with the id adAttribution
                        with(instructions.elementAt(insertIndex)) {
                            if (opcode != Opcode.INVOKE_VIRTUAL) {
                                return@forEachIndexed
                            }

                            // Hide the view
                            val viewRegister = (this as Instruction35c).registerC
                            proxy(classDef)
                                .findMutableMethodOf(method)
                                .injectHideViewCall(
                                    insertIndex,
                                    viewRegister,
                                    "Lapp/revanced/extension/youtube/patches/components/AdsFilter;",
                                    "hideAdAttributionView",
                                )
                        }
                    }
                }
            }
        }
    }
}
