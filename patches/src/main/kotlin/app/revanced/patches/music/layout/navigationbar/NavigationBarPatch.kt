package app.revanced.patches.music.layout.navigationbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal var text1 = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/NavigationBarPatch;"

@Suppress("unused")
val navigationBarPatch = bytecodePatch(
    name = "Navigation bar",
    description = "Adds options to hide navigation bar, labels and buttons."
) {
    dependsOn(
        resourceMappingPatch,
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        resourcePatch {
            execute {
                // Ensure the first ImageView has 'layout_weight' to stay properly sized
                // when the TextView is hidden.
                document("res/layout/image_with_text_tab.xml").use { document ->
                    val imageView = document.getElementsByTagName("ImageView").item(0)
                    imageView?.let {
                        if (it.attributes.getNamedItem("android:layout_weight") == null) {
                            val attr = document.createAttribute("android:layout_weight")
                            attr.value = "0.5"
                            it.attributes.setNamedItem(attr)
                        }
                    }
                }
            }
        }
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        text1 = resourceMappings["id", "text1"]

        addResources("music", "layout.navigationbar.navigationBarPatch")

        PreferenceScreen.GENERAL.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_music_navigation_bar_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_music_hide_navigation_bar_home_button"),
                    SwitchPreference("revanced_music_hide_navigation_bar_samples_button"),
                    SwitchPreference("revanced_music_hide_navigation_bar_explore_button"),
                    SwitchPreference("revanced_music_hide_navigation_bar_library_button"),
                    SwitchPreference("revanced_music_hide_navigation_bar_upgrade_button"),

                    SwitchPreference("revanced_music_hide_navigation_bar"),
                    SwitchPreference("revanced_music_hide_navigation_bar_labels"),
                )
            )
        )

        tabLayoutTextFingerprint.method.apply {
            // Hide navigation labels.
            val constIndex = indexOfFirstLiteralInstructionOrThrow(text1)
            val targetIndex = indexOfFirstInstructionOrThrow(constIndex, Opcode.CHECK_CAST)
            val targetParameter = getInstruction<ReferenceInstruction>(targetIndex).reference
            val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            if (!targetParameter.toString().endsWith("Landroid/widget/TextView;"))
                throw PatchException("Method signature parameter did not match: $targetParameter")

            addInstruction(
                targetIndex + 1,
                "invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->hideNavigationLabel(Landroid/widget/TextView;)V"
            )

            // Set navigation enum and hide navigation buttons.
            val enumIndex = tabLayoutTextFingerprint.patternMatch!!.startIndex + 3
            val enumRegister = getInstruction<OneRegisterInstruction>(enumIndex).registerA
            val insertEnumIndex = indexOfFirstInstructionOrThrow(Opcode.AND_INT_LIT8) - 2

            val pivotTabIndex = indexOfGetVisibilityInstruction(this)
            val pivotTabRegister = getInstruction<FiveRegisterInstruction>(pivotTabIndex).registerC

            addInstruction(
                pivotTabIndex,
                "invoke-static { v$pivotTabRegister }, $EXTENSION_CLASS_DESCRIPTOR->hideNavigationButton(Landroid/view/View;)V"
            )

            addInstruction(
                insertEnumIndex,
                "invoke-static { v$enumRegister }, $EXTENSION_CLASS_DESCRIPTOR->setLastAppNavigationEnum(Ljava/lang/Enum;)V"
            )
        }
    }
}
