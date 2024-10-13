package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal var drawerResourceId = -1L
    private set

private val hideInfocardsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )
    execute {
        addResources("youtube", "layout.hide.infocards.hideInfocardsResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_info_cards"),
        )

        drawerResourceId = resourceMappings[
            "id",
            "info_cards_drawer_header",
        ]
    }
}

@Suppress("unused")
val hideInfoCardsPatch = bytecodePatch(
    name = "Hide info cards",
    description = "Adds an option to hide info cards that creators add in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        lithoFilterPatch,
        hideInfocardsResourcePatch,
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

    val infocardsIncognitoParentMatch by infocardsIncognitoParentFingerprint()
    val infocardsMethodCallMatch by infocardsMethodCallFingerprint()

    execute { context ->
        infocardsIncognitoFingerprint.apply {
            match(context, infocardsIncognitoParentMatch.classDef)
        }.match!!.mutableMethod.apply {
            val invokeInstructionIndex = implementation!!.instructions.indexOfFirst {
                it.opcode.ordinal == Opcode.INVOKE_VIRTUAL.ordinal &&
                    ((it as ReferenceInstruction).reference.toString() == "Landroid/view/View;->setVisibility(I)V")
            }

            addInstruction(
                invokeInstructionIndex,
                "invoke-static {v${getInstruction<FiveRegisterInstruction>(invokeInstructionIndex).registerC}}," +
                    " Lapp/revanced/extension/youtube/patches/HideInfoCardsPatch;->hideInfoCardsIncognito(Landroid/view/View;)V",
            )
        }

        val hideInfoCardsCallMethod = infocardsMethodCallMatch.mutableMethod

        val invokeInterfaceIndex = infocardsMethodCallMatch.patternMatch!!.endIndex
        val toggleRegister = infocardsMethodCallMatch.mutableMethod.implementation!!.registerCount - 1

        hideInfoCardsCallMethod.addInstructionsWithLabels(
            invokeInterfaceIndex,
            """
                    invoke-static {}, Lapp/revanced/extension/youtube/patches/HideInfoCardsPatch;->hideInfoCardsMethodCall()Z
                    move-result v$toggleRegister
                    if-nez v$toggleRegister, :hide_info_cards
                """,
            ExternalLabel(
                "hide_info_cards",
                hideInfoCardsCallMethod.getInstruction(invokeInterfaceIndex + 1),
            ),
        )

        // Info cards can also appear as Litho components.
        val filterClassDescriptor = "Lapp/revanced/extension/youtube/patches/components/HideInfoCardsFilterPatch;"
        addLithoFilter(filterClassDescriptor)
    }
}
