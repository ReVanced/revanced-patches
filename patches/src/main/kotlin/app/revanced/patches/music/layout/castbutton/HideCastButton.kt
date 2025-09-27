package app.revanced.patches.music.layout.castbutton

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/HideCastButtonPatch;"

@Suppress("unused")
val hideCastButton = bytecodePatch(
    name = "Hide cast button",
    description = "Adds an option to hide the cast button."
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
        addResources("music", "layout.castbutton.hideCastButton")

        PreferenceScreen.GENERAL.addPreferences(
            SwitchPreference("revanced_music_hide_cast_button"),
        )

        mediaRouteButtonFingerprint.classDef.apply {
            val setVisibilityMethod = methods.first { method -> method.name == "setVisibility" }

            setVisibilityMethod.addInstructions(
                0,
                """
                    invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->hideCastButton(I)I
                    move-result p1
                """
            )
        }

        playerOverlayChipFingerprint.method.apply {
            val index = playerOverlayChipFingerprint.instructionMatches.last().index
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstruction(
                index + 1,
                "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->hideCastButton(Landroid/view/View;)V"
            )
        }
    }
}
