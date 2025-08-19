package app.revanced.patches.nunl.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hide ads and sponsored articles in list pages and remove pre-roll ads on videos.",
) {
    compatibleWith("nl.sanomamedia.android.nu")

    dependsOn(sharedExtensionPatch("nunl", mainActivityOnCreateHook))

    execute {
        // Disable video pre-roll ads.
        // Whenever the app tries to define the advertising config for JWPlayer, don't set the advertising config and directly return.
        val iputInstructionIndex = jwPlayerConfigFingerprint.method.indexOfFirstInstructionOrThrow(Opcode.IPUT_OBJECT)
        jwPlayerConfigFingerprint.method.removeInstructions(iputInstructionIndex, 1)

        // Filter injected content from API calls out of lists.
        arrayOf(screenMapperFingerprint, nextPageRepositoryImplFingerprint).forEach {
            // Index of instruction moving result of BlockPage;->getBlocks(...).
            val moveGetBlocksResultObjectIndex = it.patternMatch!!.startIndex
            it.method.apply {
                val moveInstruction = getInstruction<OneRegisterInstruction>(moveGetBlocksResultObjectIndex)

                val listRegister = moveInstruction.registerA

                // Add instruction after moving List<Block> to register and then filter this List<Block> in place.
                addInstructions(
                    moveGetBlocksResultObjectIndex + 1,
                    """
                        invoke-static { v$listRegister }, Lapp/revanced/extension/nunl/ads/HideAdsPatch;->filterAds(Ljava/util/List;)V
                    """,
                )
            }
        }
    }
}
