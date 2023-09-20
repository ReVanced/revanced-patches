package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.SetSeekBarShowTypeFingerprint
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.ShouldShowSeekBarFingerprint

@Patch(
    name = "Show seekbar",
    description = "Shows progress bar for all video.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill"),
        CompatiblePackage("com.zhiliaoapp.musically")
    ]
)
@Suppress("unused")
object ShowSeekbarPatch : BytecodePatch(setOf(SetSeekBarShowTypeFingerprint, ShouldShowSeekBarFingerprint)) {
    override fun execute(context: BytecodeContext) {
        ShouldShowSeekBarFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        SetSeekBarShowTypeFingerprint.result?.mutableMethod?.apply {
            val typeRegister = implementation!!.registerCount - 1

            addInstructions(
                0,
                """
                    const/16 v$typeRegister, 0x64
                """
            )
        } ?: throw SetSeekBarShowTypeFingerprint.exception
    }
}