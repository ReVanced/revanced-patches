package app.revanced.patches.youtube.layout.searchbar

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
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/WideSearchbarPatch;"

internal var ytWordmarkHeaderId = -1L
    private set
internal var ytPremiumWordmarkHeaderId = -1L
    private set
internal var actionBarRingoId = -1L
    private set

private val wideSearchbarResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        ytWordmarkHeaderId = resourceMappings[
            "attr",
            "ytWordmarkHeader",
        ]

        ytPremiumWordmarkHeaderId = resourceMappings[
            "attr",
            "ytPremiumWordmarkHeader",
        ]

        actionBarRingoId = resourceMappings[
            "layout",
            "action_bar_ringo",
        ]
    }
}

val wideSearchbarPatch = bytecodePatch(
    name = "Wide search bar",
    description = "Adds an option to replace the search icon with a wide search bar. " +
            "This will hide the YouTube logo when active.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        wideSearchbarResourcePatch,
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
        addResources("youtube", "layout.searchbar.wideSearchbarPatch")

        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_wide_searchbar"),
        )

        setWordmarkHeaderFingerprint.let {
            // Navigate to the method that checks if the YT logo is shown beside the search bar.
            val shouldShowLogoMethod = with(it.originalMethod) {
                val invokeStaticIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.INVOKE_STATIC &&
                            getReference<MethodReference>()?.returnType == "Z"
                }
                navigate(this).to(invokeStaticIndex).stop()
            }

            shouldShowLogoMethod.apply {
                findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructionsAtControlFlowLabel(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->enableWideSearchbar(Z)Z
                            move-result v$register
                        """
                    )
                }
            }
        }

        // Fix missing left padding when using wide searchbar.
        wideSearchbarLayoutFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Landroid/view/LayoutInflater;"
                        && reference.name == "inflate"
            }.forEach { inflateIndex ->
                val register = getInstruction<OneRegisterInstruction>(inflateIndex + 1).registerA

                addInstruction(
                    inflateIndex + 2,
                    "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->setActionBar(Landroid/view/View;)V"
                )
            }
        }
    }
}
