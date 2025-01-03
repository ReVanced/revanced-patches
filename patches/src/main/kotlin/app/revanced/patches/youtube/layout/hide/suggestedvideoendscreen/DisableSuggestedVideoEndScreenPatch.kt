package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
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
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

internal var sizeAdjustableLiteAutoNavOverlay = -1L
    private set

internal val disableSuggestedVideoEndScreenResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.suggestedvideoendscreen.disableSuggestedVideoEndScreenResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_suggested_video_end_screen"),
        )

        sizeAdjustableLiteAutoNavOverlay = resourceMappings[
            "layout",
            "size_adjustable_lite_autonav_overlay",
        ]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableSuggestedVideoEndScreenPatch;"

@Suppress("unused")
val disableSuggestedVideoEndScreenPatch = bytecodePatch(
    name = "Disable suggested video end screen",
    description = "Adds an option to disable the suggested video end screen at the end of videos.",
) {
    dependsOn(
        sharedExtensionPatch,
        disableSuggestedVideoEndScreenResourcePatch,
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
        createEndScreenViewFingerprint.method.apply {
            val addOnClickEventListenerIndex = createEndScreenViewFingerprint.filterMatches.last().index - 1
            val viewRegister = getInstruction<FiveRegisterInstruction>(addOnClickEventListenerIndex).registerC

            addInstruction(
                addOnClickEventListenerIndex + 1,
                "invoke-static {v$viewRegister}, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->closeEndScreen(Landroid/widget/ImageView;)V",
            )
        }
    }
}
