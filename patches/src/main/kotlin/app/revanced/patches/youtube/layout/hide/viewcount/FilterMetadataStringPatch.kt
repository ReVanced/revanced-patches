package app.revanced.patches.youtube.layout.hide.viewcount

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/MetadataStringHelperPatch;"

@Suppress("unused")
val filterMetadataStringPatch = bytecodePatch(
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
        addResources("youtube", "layout.hide.viewcount.filterMetadataStringPatch")

        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_view_count"),
        )
        
        rnaKMethodFingerprint.method.apply {

            val startIndex = rnaKMethodFingerprint.patternMatch?.startIndex ?: -1
            
                addInstructions(
                    startIndex+3,
                    """
                    invoke-static { v13 }, $EXTENSION_CLASS_DESCRIPTOR->filterMetadataString(Landroid/text/SpannableString;)Landroid/text/SpannableString;
                    move-result-object v13
                    """
                )
            
        }
    }
}