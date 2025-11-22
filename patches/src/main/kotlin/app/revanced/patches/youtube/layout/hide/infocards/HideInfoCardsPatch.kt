package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.getResourceId
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
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
    dependsOn(resourceMappingPatch
        )
    
    execute {

        drawerResourceId = getResourceId(
            ResourceType.ID,
            "info_cards_drawer_header",
        )
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
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        )
    )

    execute {
        addResources("youtube", "layout.hide.infocards.hideInfocardsResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_info_cards"),
        )

        // Edit: This old non litho code may be obsolete and no longer used by any supported versions.
        infocardsIncognitoFingerprint.match(infocardsIncognitoParentFingerprint.originalClassDef).method.apply {
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

        // Edit: This old non litho code may be obsolete and no longer used by any supported versions.
        infocardsMethodCallFingerprint.let {
            val invokeInterfaceIndex = it.instructionMatches.last().index
            it.method.apply {
                val register = implementation!!.registerCount - 1

                addInstructionsWithLabels(
                    invokeInterfaceIndex,
                    """
                        invoke-static {}, Lapp/revanced/extension/youtube/patches/HideInfoCardsPatch;->hideInfoCardsMethodCall()Z
                        move-result v$register
                        if-nez v$register, :hide_info_cards
                    """,
                    ExternalLabel(
                        "hide_info_cards",
                        getInstruction(invokeInterfaceIndex + 1),
                    )
                )
            }
        }

        // Info cards can also appear as Litho components.
        val filterClassDescriptor = "Lapp/revanced/extension/youtube/patches/components/HideInfoCardsFilter;"
        addLithoFilter(filterClassDescriptor)
    }
}
