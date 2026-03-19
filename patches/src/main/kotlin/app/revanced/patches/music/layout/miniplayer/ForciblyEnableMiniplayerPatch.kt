@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.music.layout.miniplayer

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/music/patches/ForciblyEnableMiniplayerPatch;"

@Suppress("unused")
val forciblyEnableMiniplayerPatch = bytecodePatch(
    name = "Forcibly enable miniplayer",
    description = "Adds an option to forcibly enable the miniplayer when switching between music videos, podcasts, or songs."
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
            "8.37.56",
            "8.40.54",
            "8.44.54"
        ),
    )

    apply {
        addResources("music", "layout.miniplayer.forciblyEnableMiniplayer")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_music_forcibly_enable_miniplayer")
        )

        minimizedPlayerMethod.apply {
            val invokeIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL && methodReference?.name == "booleanValue"
            }

            val moveResultIndex = invokeIndex + 1
            val moveResultInstr = getInstruction<OneRegisterInstruction>(moveResultIndex)
            val targetRegister = moveResultInstr.registerA

            addInstructions(
                moveResultIndex + 1,
                """
                    invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->forciblyEnableMiniplayerPatch(Z)Z
                    move-result v$targetRegister
                """
            )
        }
    }
}