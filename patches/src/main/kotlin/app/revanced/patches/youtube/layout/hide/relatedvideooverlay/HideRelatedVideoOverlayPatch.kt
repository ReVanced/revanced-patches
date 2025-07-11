package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patcher.util.smali.ExternalLabel

internal var appRelatedEndScreenResults = -1L
    private set

private val hideRelatedVideoOverlayResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
    )

    execute {
        appRelatedEndScreenResults = resourceMappings[
            "layout",
            "app_related_endscreen_results",
        ]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HideRelatedVideoOverlayPatch;"

@Suppress("unused")
val hideRelatedVideoOverlayPatch = bytecodePatch(
    name = "Hide related video overlay",
    description = "Adds an option to hide the related video overlay shown when swiping up in fullscreen.",
) {
    dependsOn(
        settingsPatch,
        sharedExtensionPatch,
        addResourcesPatch,
        hideRelatedVideoOverlayResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "layout.hide.relatedvideooverlay.hideRelatedVideoOverlayPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_related_videos_overlay")
        )

        relatedEndScreenResultsFingerprint.match(
            relatedEndScreenResultsParentFingerprint.originalClassDef
        ).method.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideRelatedVideoOverlay()Z
                    move-result v0
                    if-eqz v0, :show
                    return-void
                """,
                ExternalLabel("show", getInstruction(0))
            )
        }
    }
}
