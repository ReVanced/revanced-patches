package app.revanced.patches.youtube.layout.hide.fullscreenambientmode

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableFullscreenAmbientModePatch;"

val disableFullscreenAmbientModePatch = bytecodePatch(
    name = "Disable fullscreen ambient mode",
    description = "Adds an option to disable the ambient mode when in fullscreen.",
) {
    dependsOn(
        settingsPatch,
        sharedExtensionPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.hide.fullscreenambientmode.disableFullscreenAmbientModePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_fullscreen_ambient_mode"),
        )

        setFullScreenBackgroundColorFingerprint.method.apply {
            val insertIndex = indexOfFirstInstructionReversedOrThrow {
                getReference<MethodReference>()?.name == "setBackgroundColor"
            }
            val register = getInstruction<FiveRegisterInstruction>(insertIndex).registerD

            addInstructions(
                insertIndex,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getFullScreenBackgroundColor(I)I
                    move-result v$register
                """,
            )
        }
    }
}
