package app.revanced.patches.youtube.layout.player.background

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
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
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/RemovePlayerControlsBackgroundPatch;"

internal var youtubeControlsButtonGroupLayoutStubResId = -1L
    private set

private val playerControlsBackgroundResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        youtubeControlsButtonGroupLayoutStubResId = resourceMappings["id", "youtube_controls_button_group_layout_stub"]
    }
}

@Suppress("unused")
val playerControlsBackgroundPatch = bytecodePatch(
    name = "Remove player control buttons background",
    description = "Removes the dark background surrounding the video player control buttons.",
) {
    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        playerControlsBackgroundResourcePatch
    )

    execute {
        addResources("youtube", "layout.player.background.playerControlsBackgroundPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_remove_player_control_buttons_background"),
        )

        inflateControlsGroupLayoutStubFingerprint.method.apply {
            val controlsButtonGroupLayoutStubResIdConstIndex =
                indexOfFirstLiteralInstructionOrThrow(youtubeControlsButtonGroupLayoutStubResId)
            val inflateControlsGroupLayoutStubIndex =
                indexOfFirstInstruction(controlsButtonGroupLayoutStubResIdConstIndex) {
                    getReference<MethodReference>()?.name == "inflate"
                }

            val freeRegister = findFreeRegister(inflateControlsGroupLayoutStubIndex)
            val removePlayerControlButtonsBackgroundDescriptor =
                "$EXTENSION_CLASS_DESCRIPTOR->removePlayerControlButtonsBackground(Landroid/view/View;)V"

            addInstructions(
                inflateControlsGroupLayoutStubIndex + 1,
                """
                   # Move the inflated layout to a temporary register.
                   # The result of the inflate method is by default not moved to a register after the method is called.
                   move-result-object v$freeRegister
                   invoke-static { v$freeRegister }, $removePlayerControlButtonsBackgroundDescriptor
                """
            )
        }
    }
}
