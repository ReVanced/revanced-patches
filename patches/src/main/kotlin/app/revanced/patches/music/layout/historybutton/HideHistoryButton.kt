package app.revanced.patches.music.layout.historybutton

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
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

internal var historyMenuItem = -1L
    private set
internal var offlineSettingsMenuItem = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/HideHistoryButtonPatch;"

@Suppress("unused")
val hideHistoryButton = bytecodePatch(
    name = "Hide history button",
    description = "Adds an option to hide the history button."
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
        historyMenuItem = resourceMappings["id", "history_menu_item"]
        offlineSettingsMenuItem = resourceMappings["id", "offline_settings_menu_item"]

        addResources("music", "layout.historybutton.hideHistoryButton")

        PreferenceScreen.GENERAL.addPreferences(
            SwitchPreference("revanced_music_hide_history_button"),
        )

        setOf(
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
    }
}
