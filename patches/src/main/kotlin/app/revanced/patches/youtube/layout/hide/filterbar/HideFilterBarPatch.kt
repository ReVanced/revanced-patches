package app.revanced.patches.youtube.layout.hide.filterbar

import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal var filterBarHeightId = -1L
    private set
internal var relatedChipCloudMarginId = -1L
    private set
internal var barContainerHeightId = -1L
    private set

private val hideFilterBarResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.filterbar.hideFilterBarResourcePatch")

        PreferenceScreen.FEED.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_hide_filter_bar_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_filter_bar_feed_in_feed"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_search"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_related_videos"),
                ),
            ),
        )

        relatedChipCloudMarginId = resourceMappings["layout", "related_chip_cloud_reduced_margins"]
        filterBarHeightId = resourceMappings["dimen", "filter_bar_height"]
        barContainerHeightId = resourceMappings["dimen", "bar_container_height"]
    }
}

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/HideFilterBarPatch;"

@Suppress("unused")
val hideFilterBarPatch = bytecodePatch(
    name = "Hide filter bar",
    description = "Adds options to hide the category bar at the top of video feeds.",
) {
    dependsOn(
        sharedExtensionPatch,
        hideFilterBarResourcePatch,
    )

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
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val filterBarHeightMatch by filterBarHeightFingerprint()
    val relatedChipCloudMatch by relatedChipCloudFingerprint()
    val searchFingerprintResultsChipBarMatch by searchResultsChipBarFingerprint()

    execute {
        fun <RegisterInstruction : OneRegisterInstruction> Match.patch(
            insertIndexOffset: Int = 0,
            hookRegisterOffset: Int = 0,
            instructions: (Int) -> String,
        ) = mutableMethod.apply {
            val endIndex = patternMatch!!.endIndex
            val insertIndex = endIndex + insertIndexOffset
            val register = getInstruction<RegisterInstruction>(endIndex + hookRegisterOffset).registerA

            addInstructions(insertIndex, instructions(register))
        }

        filterBarHeightMatch.patch<TwoRegisterInstruction> { register ->
            """
                invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->hideInFeed(I)I
                move-result v$register
            """
        }

        relatedChipCloudMatch.patch<OneRegisterInstruction>(1) { register ->
            "invoke-static { v$register }, " +
                "$EXTENSION_CLASS_DESCRIPTOR->hideInRelatedVideos(Landroid/view/View;)V"
        }

        searchFingerprintResultsChipBarMatch.patch<OneRegisterInstruction>(-1, -2) { register ->
            """
                invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->hideInSearch(I)I
                move-result v$register
            """
        }
    }
}
