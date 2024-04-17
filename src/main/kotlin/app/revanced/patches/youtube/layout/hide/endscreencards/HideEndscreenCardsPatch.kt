package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints.LayoutCircleFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints.LayoutIconFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints.LayoutVideoFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

@Patch(
    name = "Hide endscreen cards",
    description = "Adds an option to hide suggested video cards at the end of videos.",
    dependencies = [
        IntegrationsPatch::class,
        HideEndscreenCardsResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object HideEndscreenCardsPatch : BytecodePatch(
    setOf(
        LayoutCircleFingerprint,
        LayoutIconFingerprint,
        LayoutVideoFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        fun MethodFingerprint.injectHideCall() {
            val layoutResult = result ?: throw exception
            layoutResult.mutableMethod.apply {
                val insertIndex = layoutResult.scanResult.patternScanResult!!.endIndex + 1
                val viewRegister = getInstruction<Instruction21c>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, Lapp/revanced/integrations/youtube/patches/HideEndscreenCardsPatch;->hideEndscreen(Landroid/view/View;)V"
                )
            }
        }

        listOf(
            LayoutCircleFingerprint,
            LayoutIconFingerprint,
            LayoutVideoFingerprint
        ).forEach(MethodFingerprint::injectHideCall)
    }
}
