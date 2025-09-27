package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var layoutCircle = -1L
    private set
internal var layoutIcon = -1L
    private set
internal var layoutVideo = -1L
    private set

private val hideEndscreenCardsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.endscreencards.hideEndscreenCardsResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_endscreen_cards"),
        )

        fun idOf(name: String) = resourceMappings["layout", "endscreen_element_layout_$name"]

        layoutCircle = idOf("circle")
        layoutIcon = idOf("icon")
        layoutVideo = idOf("video")
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HideEndscreenCardsPatch;"

@Suppress("unused")
val hideEndscreenCardsPatch = bytecodePatch(
    name = "Hide end screen cards",
    description = "Adds an option to hide suggested video cards at the end of videos.",
) {
    dependsOn(
        sharedExtensionPatch,
        hideEndscreenCardsResourcePatch,
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
        listOf(
            layoutCircleFingerprint,
            layoutIconFingerprint,
            layoutVideoFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                val insertIndex = fingerprint.patternMatch!!.endIndex + 1
                val viewRegister = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, $EXTENSION_CLASS_DESCRIPTOR->hideEndscreen(Landroid/view/View;)V",
                )
            }
        }
    }
}
