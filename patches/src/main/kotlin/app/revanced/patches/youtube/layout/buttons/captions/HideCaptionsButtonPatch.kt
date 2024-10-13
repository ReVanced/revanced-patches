package app.revanced.patches.youtube.layout.buttons.captions

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.autocaptions.subtitleButtonControllerFingerprint
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val hideCaptionsButtonPatch = bytecodePatch(
    name = "Hide captions button",
    description = "Adds an option to hide the captions button in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val subtitleButtonControllerMatch by subtitleButtonControllerFingerprint()

    execute {
        addResources("youtube", "layout.buttons.captions.hideCaptionsButtonPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_captions_button"),
        )

        val subtitleButtonControllerMethod = subtitleButtonControllerMatch.mutableMethod

        // Due to previously applied patches, scanResult index cannot be used in this context
        val insertIndex = subtitleButtonControllerMethod.implementation!!.instructions.indexOfFirst {
            it.opcode == Opcode.IGET_BOOLEAN
        } + 1

        subtitleButtonControllerMethod.addInstruction(
            insertIndex,
            """
                invoke-static {v0}, Lapp/revanced/extension/youtube/patches/HideCaptionsButtonPatch;->hideCaptionsButton(Landroid/widget/ImageView;)V
            """,
        )
    }
}
