package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

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
            "7.29.52"
        )
    )

    execute {
        addResources("music", "layout.compactheader.hideCategoryBar")

        PreferenceScreen.GENERAL.addPreferences(
            SwitchPreference("revanced_music_hide_category_bar"),
        )

        constructCategoryBarFingerprint.method.apply {
            val insertIndex = constructCategoryBarFingerprint.patternMatch!!.startIndex
            val register = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
            val freeRegister = findFreeRegister(insertIndex, register)

            addInstructionsWithLabels(
                insertIndex,
                """
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->hideCategoryBar()Z
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :show
                    const/16 v$freeRegister, 0x8
                    invoke-virtual { v$register, v$freeRegister }, Landroid/view/View;->setVisibility(I)V
                    :show
                    nop
                """
            )
        }
    }
}
