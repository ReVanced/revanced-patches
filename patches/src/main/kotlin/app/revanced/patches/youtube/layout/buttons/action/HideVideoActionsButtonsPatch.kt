package app.revanced.patches.youtube.layout.buttons.action

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playservice.is_20_22_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import java.util.logging.Logger

@Suppress("unused")
val hideVideoActionButtonsPatch = resourcePatch(
    name = "Hide video action buttons",
    description = "Adds options to hide action buttons (such as the Download button) under videos. " +
            "Patching version 20.21.37 or lower can hide more player button types."
) {
    dependsOn(
        resourceMappingPatch,
        lithoFilterPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "layout.buttons.action.hideButtonsPatch")

        val preferences = mutableSetOf(
            SwitchPreference("revanced_disable_like_subscribe_glow"),
            SwitchPreference("revanced_hide_download_button"),
            SwitchPreference("revanced_hide_like_dislike_button"),
            SwitchPreference("revanced_hide_comments_button"),
            SwitchPreference("revanced_hide_save_button"),
            SwitchPreference("revanced_hide_remix_button"),
            SwitchPreference("revanced_hide_share_button"),

            )

        // 20.22+ cannot hide all action buttons because of buffer changes.
        if (!is_20_22_or_greater) {
            preferences.addAll(
                listOf(
                    SwitchPreference("revanced_hide_hype_button"),
                    SwitchPreference("revanced_hide_ask_button"),
                    SwitchPreference("revanced_hide_clip_button"),
                    SwitchPreference("revanced_hide_promote_button"),
                    SwitchPreference("revanced_hide_report_button"),
                    SwitchPreference("revanced_hide_shop_button"),
                    SwitchPreference("revanced_hide_stop_ads_button"),
                    SwitchPreference("revanced_hide_thanks_button"),
                ),
            )
        }

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                "revanced_hide_buttons_screen",
                preferences = preferences,
            ),
        )

        addLithoFilter("Lapp/revanced/extension/youtube/patches/litho/VideoActionButtonsFilter;")
    }
}
