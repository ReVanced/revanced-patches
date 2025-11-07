package app.revanced.patches.music.layout.notificationbutton

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
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
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var topBarMenuItemImageView = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/HideNotificationButtonPatch;"

@Suppress("unused")
val hideNotificationButton = bytecodePatch(
    name = "Hide notification button",
    description = "Adds an option to hide the notification button."
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
        topBarMenuItemImageView = resourceMappings["id", "top_bar_menu_item_image_view"]

        addResources("music", "layout.notificationbutton.hideNotificationButton")

        PreferenceScreen.GENERAL.addPreferences(
            SwitchPreference("revanced_music_hide_notification_button"),
        )

        topBarMenuItemImageViewFingerprint.method.apply {
            val resourceIndex = indexOfFirstLiteralInstructionOrThrow(topBarMenuItemImageView)
            val targetIndex = indexOfFirstInstructionOrThrow(resourceIndex, Opcode.MOVE_RESULT_OBJECT)
            val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstruction(
                targetIndex + 1,
                "invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->hideNotificationButton(Landroid/view/View;)V"
            )
        }
    }
}
