package app.revanced.patches.nunl.ads

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("ObjectPropertyName")
val `Hide ads` by creatingBytecodePatch(
    description = "Hide ads and sponsored articles in list pages and remove pre-roll ads on videos.",
) {
    compatibleWith("nl.sanomamedia.android.nu")

    dependsOn(sharedExtensionPatch("nunl", mainActivityOnCreateHook))

    apply {
        // Disable video pre-roll ads.
        // Whenever the app tries to define the advertising config for JWPlayer, don't set the advertising config and directly return.
        val iputInstructionIndex = jwPlayerConfigMethod.indexOfFirstInstructionOrThrow(Opcode.IPUT_OBJECT)
        jwPlayerConfigMethod.removeInstructions(iputInstructionIndex, 1)

        // Filter injected content from API calls out of lists.
        arrayOf(screenMapperMethodMatch, nextPageRepositoryImplMethodMatch).forEach { match ->
            // Index of instruction moving result of BlockPage;->getBlocks(...).
            val moveGetBlocksResultObjectIndex = match.indices.first()
            val moveInstruction = match.method.getInstruction<OneRegisterInstruction>(moveGetBlocksResultObjectIndex)

            val listRegister = moveInstruction.registerA

            // Add instruction after moving List<Block> to register and then filter this List<Block> in place.
            match.method.addInstructions(
                moveGetBlocksResultObjectIndex + 1,
                """
                        invoke-static { v$listRegister }, Lapp/revanced/extension/nunl/ads/HideAdsPatch;->filterAds(Ljava/util/List;)V
                    """,
            )
        }
    }
}
