package app.revanced.patches.music.layout.buttons

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var playerOverlayChip = -1L
    private set
internal var historyMenuItem = -1L
    private set
internal var offlineSettingsMenuItem = -1L
    private set
internal var searchButton = -1L
    private set
internal var topBarMenuItemImageView = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/HideButtonsPatch;"

@Suppress("unused")
val hideButtons = bytecodePatch(
    name = "Hide buttons",
    description = "Adds options to hide the cast, history, notification, and search buttons."
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        resourceMappingPatch
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        playerOverlayChip = resourceMappings["id", "player_overlay_chip"]
        historyMenuItem = resourceMappings["id", "history_menu_item"]
        offlineSettingsMenuItem = resourceMappings["id", "offline_settings_menu_item"]
        searchButton = resourceMappings["layout", "search_button"]
        topBarMenuItemImageView = resourceMappings["id", "top_bar_menu_item_image_view"]

        addResources("music", "layout.buttons.hideButtons")

        PreferenceScreen.GENERAL.addPreferences(
            SwitchPreference("revanced_music_hide_cast_button"),
            SwitchPreference("revanced_music_hide_history_button"),
            SwitchPreference("revanced_music_hide_notification_button"),
            SwitchPreference("revanced_music_hide_search_button")
        )

        // Region for hide history button in the top bar.
        arrayOf(
            historyMenuItemFingerprint,
            historyMenuItemOfflineTabFingerprint
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                val targetIndex = fingerprint.patternMatch!!.startIndex
                val targetRegister = getInstruction<FiveRegisterInstruction>(targetIndex).registerD

                addInstructions(
                    targetIndex,
                    """
                        invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->hideHistoryButton(Z)Z
                        move-result v$targetRegister
                    """
                )
            }
        }

        // Region for hide cast, search and notification buttons in the top bar.
        arrayOf(
            Triple(playerOverlayChipFingerprint, playerOverlayChip, "hideCastButton"),
            Triple(searchActionViewFingerprint, searchButton, "hideSearchButton"),
            Triple(topBarMenuItemImageViewFingerprint, topBarMenuItemImageView, "hideNotificationButton")
        ).forEach { (fingerprint, resourceIdLiteral, methodName) ->
            fingerprint.method.apply {
                val resourceIndex = indexOfFirstLiteralInstructionOrThrow(resourceIdLiteral)
                val targetIndex = indexOfFirstInstructionOrThrow(
                    resourceIndex, Opcode.MOVE_RESULT_OBJECT
                )
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex + 1,
                    "invoke-static { v$targetRegister }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->$methodName(Landroid/view/View;)V"
                )
            }
        }

        // Region for hide cast button in the player.
        mediaRouteButtonFingerprint.classDef.methods.single { method ->
            method.name == "setVisibility"
        }.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->hideCastButton(I)I
                move-result p1
            """
        )
    }
}
