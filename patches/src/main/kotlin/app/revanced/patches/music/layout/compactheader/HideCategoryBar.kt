package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var chipCloud = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/HideCategoryBarPatch;"

@Suppress("unused")
val hideCategoryBar = bytecodePatch(
    name = "Hide category bar",
    description = "Adds an option to hide the category bar at the top of the homepage."
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        chipCloud = resourceMappings["layout", "chip_cloud"]

        addResources("music", "layout.compactheader.hideCategoryBar")

        PreferenceScreen.GENERAL.addPreferences(
            SwitchPreference("revanced_music_hide_category_bar"),
        )

        chipCloudFingerprint.method.apply {
            val targetIndex = chipCloudFingerprint.patternMatch!!.endIndex
            val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstruction(
                targetIndex + 1,
                "invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->hideCategoryBar(Landroid/view/View;)V"
            )
        }
    }
}
