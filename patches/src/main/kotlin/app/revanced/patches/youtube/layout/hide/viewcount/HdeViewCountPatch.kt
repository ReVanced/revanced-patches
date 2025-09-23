package app.revanced.patches.youtube.layout.hide.viewcount

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/HideViewCountPatch;"

@Suppress("unused")
val hideViewCountPatch = bytecodePatch(
    name = "Hide View Count",
    description = "Hide the view count from the video feed list.",
) {
    dependsOn(
        settingsPatch,
        addResourcesPatch
    ) 

    compatibleWith(
        "com.google.android.youtube"(
           "20.12.46", 
            "20.13.41",
        )
    ) 

    execute {
        addResources("youtube", "layout.hide.viewcount.hideViewCountPatch")

        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_view_count"),
        )
        
        hideViewCountFingerprint.method.apply {

            val startIndex = hideViewCountFingerprint.patternMatch?.startIndex ?: -1

            // Inject the code at the identified position
            // A float value is passed which is used to determine text
            addInstructions(
                startIndex + 14,
                """
                invoke-static {v2, v0}, $EXTENSION_CLASS_DESCRIPTOR->hideViewCount(Landroid/text/SpannableString;F)Landroid/text/SpannableString;
                move-result-object v2
                """
            )
        }
    }
}