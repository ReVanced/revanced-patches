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
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

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

@Suppress("unused")
val hideEndscreenCardsPatch = bytecodePatch(
    name = "Hide endscreen cards",
    description = "Adds an option to hide suggested video cards at the end of videos.",
) {
    dependsOn(
        sharedExtensionPatch,
        hideEndscreenCardsResourcePatch,
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

    val layoutCircleMatch by layoutCircleFingerprint()
    val layoutIconMatch by layoutIconFingerprint()
    val layoutVideoMatch by layoutVideoFingerprint()

    execute {
        listOf(
            layoutCircleMatch,
            layoutIconMatch,
            layoutVideoMatch,
        ).forEach {
            it.mutableMethod.apply {
                val insertIndex = it.patternMatch!!.endIndex + 1
                val viewRegister = getInstruction<Instruction21c>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, " +
                        "Lapp/revanced/extension/youtube/patches/HideEndscreenCardsPatch;->" +
                        "hideEndscreen(Landroid/view/View;)V",
                )
            }
        }
    }
}
