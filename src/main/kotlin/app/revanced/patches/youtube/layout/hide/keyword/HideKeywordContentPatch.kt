package app.revanced.patches.youtube.layout.hide.keyword

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Hide keyword content",
    description = "Adds options to hide home feed or search results based on keywords",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, LithoFilterPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.35",
                "19.03.36",
                "19.04.37"
            ]
        )
    ]
)
@Suppress("unused")
object HideKeywordContentPatch : BytecodePatch(emptySet()) {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/HideKeywordContentFilter;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                key = "revanced_hide_keyword_content_preference_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_keyword_content"),
                    TextPreference("revanced_hide_keyword_content_phrases", inputType = InputType.TEXT_MULTI_LINE),
                ),
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
