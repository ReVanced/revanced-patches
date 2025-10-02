package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
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
import app.revanced.patches.youtube.misc.playservice.is_19_43_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var layoutCircle = -1L
    private set
internal var layoutIcon = -1L
    private set
internal var layoutVideo = -1L
    private set

private val hideEndScreenCardsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.endscreencards.hideEndScreenCardsResourcePatch")

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
    "Lapp/revanced/extension/youtube/patches/HideEndScreenCardsPatch;"

@Suppress("unused")
val hideEndScreenCardsPatch = bytecodePatch(
    name = "Hide end screen cards",
    description = "Adds an option to hide suggested video cards at the end of videos.",
) {
    dependsOn(
        sharedExtensionPatch,
        hideEndScreenCardsResourcePatch,
        versionCheckPatch
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
                    "invoke-static { v$viewRegister }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->hideEndScreenCardView(Landroid/view/View;)V",
                )
            }
        }

        if (is_19_43_or_greater) {
            showEndscreenCardsFingerprint.method.addInstructionsWithLabels(
                0,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideEndScreenCards()Z
                    move-result v0
                    if-eqz v0, :show
                    return-void
                    :show
                    nop
                """
            )
        }
    }
}
