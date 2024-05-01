package app.revanced.patches.youtube.layout.hide.crowdfundingbox

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
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val crowdfundingBoxPatch = bytecodePatch(
    name = "Hide crowdfunding box",
    description = "Adds an option to hide the crowdfunding box between the player and video description.",
) {
    var crowdfundingBoxId = -1L

    dependsOn(
        integrationsPatch,
        resourcePatch {
            dependsOn(
                settingsPatch,
                resourceMappingPatch,
                addResourcesPatch,
            )

            execute {
                addResources("youtube", "layout.hide.crowdfundingbox.CrowdfundingBoxResourcePatch")

                PreferenceScreen.FEED.addPreferences(
                    SwitchPreference("revanced_hide_crowdfunding_box"),
                )

                crowdfundingBoxId = resourceMappings[
                    "layout",
                    "donation_companion",
                ]
            }
        },
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
        ),
    )

    val createCrowdfundingBoxResult by literalValueFingerprint(literalSupplier = { crowdfundingBoxId }) {
        accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
        opcodes(
            Opcode.INVOKE_VIRTUAL,
            Opcode.MOVE_RESULT_OBJECT,
            Opcode.IPUT_OBJECT,
        )
    }

    execute {
        createCrowdfundingBoxResult.mutableMethod.apply {
            val insertIndex = createCrowdfundingBoxResult.scanResult.patternScanResult!!.endIndex
            val objectRegister = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

            addInstruction(
                insertIndex,
                "invoke-static {v$objectRegister}, " +
                    "Lapp/revanced/integrations/youtube/patches/HideCrowdfundingBoxPatch;->" +
                    "hideCrowdfundingBox(Landroid/view/View;)V",
            )
        }
    }
}
