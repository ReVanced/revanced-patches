package app.revanced.patches.youtube.layout.hide.comments

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.addFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val commentsPatch = resourcePatch(
    name = "Comments",
    description = "Adds options to hide components related to comments.",
) {
    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
        ),
    )

    dependsOn(
        settingsPatch,
        lithoFilterPatch,
        addResourcesPatch,
    )

    val filterClassDescriptor =
        "Lapp/revanced/integrations/youtube/patches/components/CommentsFilter;"

    execute {
        addResources(this)

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                "revanced_comments_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_preview_comment"),
                    SwitchPreference("revanced_hide_comments_section"),
                    SwitchPreference("revanced_hide_comment_timestamp_and_emoji_buttons"),
                ),
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            ),
        )

        addFilter(filterClassDescriptor)
    }
}
