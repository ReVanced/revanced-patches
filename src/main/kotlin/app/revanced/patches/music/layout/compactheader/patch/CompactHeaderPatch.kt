package app.revanced.patches.music.layout.compactheader.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.annotations.MusicCompatibility
import app.revanced.patches.music.layout.compactheader.fingerprints.CompactHeaderConstructorFingerprint
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x

@Patch(false)
@Name("Compact header")
@Description("Hides the music category bar at the top of the homepage.")
@MusicCompatibility
class CompactHeaderPatch : BytecodePatch(
    listOf(CompactHeaderConstructorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val result = CompactHeaderConstructorFingerprint.result!!
        val method = result.mutableMethod

        val insertIndex = result.scanResult.patternScanResult!!.endIndex
        val register = (method.implementation!!.instructions[insertIndex - 1] as BuilderInstruction11x).registerA
        method.addInstructions(
            insertIndex, """
                const/16 v2, 0x8
                invoke-virtual {v${register}, v2}, Landroid/view/View;->setVisibility(I)V
            """
        )
    }
}
