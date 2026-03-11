package app.revanced.patches.youtube.layout.hide.endscreensuggestedvideo

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.firstMethod
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HideEndScreenSuggestedVideoPatch;"

@Suppress("unused")
val hideEndScreenSuggestedVideoPatch = bytecodePatch(
    name = "Hide end screen suggested video",
    description = "Adds an option to hide the suggested video at the end of videos.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45",
            "20.44.38"
        ),
    )

    apply {
        addResources(
            "youtube",
            "layout.hide.endscreensuggestedvideo.hideEndScreenSuggestedVideoPatch"
        )

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_end_screen_suggested_video"),
        )


        val autoNavStatusMethod =
            autoNavConstructorMethod.immutableClassDef.getAutoNavStatusMethod()

        val endScreenMethod = removeOnLayoutChangeListenerMethodMatch.let {
            firstMethod(it.method.getInstruction<ReferenceInstruction>(it[1]).methodReference!!)
        }


        getEndScreenSuggestedVideoMethodMatch(autoNavStatusMethod).let { match ->
            match.method.apply {
                val autoNavField = getInstruction(match[0]).fieldReference!!

                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideEndScreenSuggestedVideo()Z
                        move-result v0
                        if-eqz v0, :show_end_screen_recommendation

                        iget-object v0, p0, $autoNavField

                        # This reference checks whether autoplay is turned on.
                        invoke-virtual { v0 }, $autoNavStatusMethod
                        move-result v0

                        # Hide suggested video end screen only when autoplay is turned off.
                        if-nez v0, :show_end_screen_recommendation
                        return-void
                    """,
                    ExternalLabel("show_end_screen_recommendation", getInstruction(0)),
                )
            }
        }
    }
}
