package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.player.overlay.fingerprints.createPlayerOverviewFingerprint
import app.revanced.util.indexOfFirstWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/CustomPlayerOverlayOpacityPatch;"

@Suppress("unused")
val customPlayerOverlayOpacityPatch = bytecodePatch(
    name = "Custom player overlay opacity",
    description = "Adds an option to change the opacity of the video player background when player controls are visible.",
) {
    dependsOn(customPlayerOverlayOpacityResourcePatch)

    compatibleWith("com.google.android.youtube")

    val createPlayerOverviewResult by createPlayerOverviewFingerprint

    execute {
        createPlayerOverviewResult.mutableMethod.apply {
            val viewRegisterIndex =
                indexOfFirstWideLiteralInstructionValue(scrimOverlayId) + 3
            val viewRegister =
                getInstruction<OneRegisterInstruction>(viewRegisterIndex).registerA

            val insertIndex = viewRegisterIndex + 1
            addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->changeOpacity(Landroid/widget/ImageView;)V",
            )
        }
    }
}
