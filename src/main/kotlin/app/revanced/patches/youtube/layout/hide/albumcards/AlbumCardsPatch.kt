package app.revanced.patches.youtube.layout.hide.albumcards

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.albumcards.fingerprints.AlbumCardsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide album cards",
    description = "Adds an option to hide album cards below artist descriptions.",
    dependencies = [
        IntegrationsPatch::class,
        AlbumCardsResourcePatch::class
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
object AlbumCardsPatch : BytecodePatch(
    setOf(AlbumCardsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AlbumCardsFingerprint.result?.let {
            it.mutableMethod.apply {
                val checkCastAnchorIndex = it.scanResult.patternScanResult!!.endIndex
                val insertIndex = checkCastAnchorIndex + 1

                val albumCardViewRegister = getInstruction<OneRegisterInstruction>(checkCastAnchorIndex).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static {v$albumCardViewRegister}, " +
                            "Lapp/revanced/integrations/youtube/patches/HideAlbumCardsPatch;" +
                            "->" +
                            "hideAlbumCard(Landroid/view/View;)V"
                )
            }
        } ?: throw AlbumCardsFingerprint.exception
    }
}
