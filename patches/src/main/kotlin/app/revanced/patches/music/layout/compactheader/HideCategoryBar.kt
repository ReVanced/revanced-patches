package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var chipCloud = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/HideCategoryBarPatch;"

@Suppress("unused", "ObjectPropertyName")
val `Hide category bar` by creatingBytecodePatch(
    description = "Adds an option to hide the category bar at the top of the homepage.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52",
        ),
    )

    apply {
        addResources("music", "layout.compactheader.hideCategoryBar")

        PreferenceScreen.GENERAL.addPreferences(
            SwitchPreference("revanced_music_hide_category_bar"),
        )

        chipCloud = ResourceType.LAYOUT["chip_cloud"]

        chipCloudMethod.apply {
            val targetIndex = chipCloudMethod.patternMatch.endIndex
            val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstruction(
                targetIndex + 1,
                "invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->hideCategoryBar(Landroid/view/View;)V",
            )
        }
    }
}
