package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.wideLiteral
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.fix.verticalscroll.verticalScrollPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.getResourceId
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.ad.getpremium.hideGetPremiumPatch
import app.revanced.patches.youtube.misc.fix.backtoexitgesture.fixBackToExitGesturePatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.forEachInstructionAsSequence
import app.revanced.util.injectHideViewCall
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

internal var adAttributionId = -1L
    private set
internal var fullScreenEngagementAdContainer = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/components/AdsFilter;"

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
            SwitchPreference("revanced_hide_creator_store_shelf"),
            SwitchPreference("revanced_hide_end_screen_store_banner"),
            SwitchPreference("revanced_hide_fullscreen_ads"),
            SwitchPreference("revanced_hide_general_ads"),
            SwitchPreference("revanced_hide_merchandise_banners"),
            SwitchPreference("revanced_hide_paid_promotion_label"),
            SwitchPreference("revanced_hide_self_sponsor_ads"),
            SwitchPreference("revanced_hide_shopping_links"),
            SwitchPreference("revanced_hide_view_products_banner"),
            SwitchPreference("revanced_hide_web_search_results"),
        )

        addLithoFilter("Lapp/revanced/extension/youtube/patches/components/AdsFilter;")

        adAttributionId = getResourceId(ResourceType.ID, "ad_attribution")
        fullScreenEngagementAdContainer = getResourceId(ResourceType.ID, "fullscreen_engagement_ad_container")
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
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        )
    )

    execute {
        // Hide end screen store banner.

        fullScreenEngagementAdContainerFingerprint.method.apply {
            val addListIndex = indexOfAddListInstruction(this)
            val addListInstruction = getInstruction<FiveRegisterInstruction>(addListIndex)
            val listRegister = addListInstruction.registerC
            val objectRegister = addListInstruction.registerD

            replaceInstruction(
                addListIndex,
                "invoke-static { v$listRegister, v$objectRegister }, $EXTENSION_CLASS_DESCRIPTOR" +
                        "->hideEndScreenStoreBanner(Ljava/util/List;Ljava/lang/Object;)V"
            )
        }

        // Hide ad views.

        forEachInstructionAsSequence { _, method, index, instruction ->
            if (instruction.opcode != Opcode.CONST) return@forEachInstructionAsSequence
            if (instruction.wideLiteral != adAttributionId) return@forEachInstructionAsSequence


            val insertIndex = index + 1

            // Call to get the view with the id adAttribution,
            if (method.instructions[insertIndex].opcode != Opcode.INVOKE_VIRTUAL) return@forEachInstructionAsSequence
            val viewRegister = method.getInstruction<FiveRegisterInstruction>(insertIndex).registerC
            method.injectHideViewCall(insertIndex, viewRegister, EXTENSION_CLASS_DESCRIPTOR, "hideAdAttributionView")
        }
    }
}
