package app.revanced.patches.nunl.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hide ads and sponsored articles in list pages and remove pre-roll ads on videos.",
) {
    extendWith("extensions/nunl.rve")

    compatibleWith("nl.sanomamedia.android.nu"("11.0.0"))

    execute {
        // prevent video pre-roll ads
        jwCreateAdvertisementFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig${'$'}Builder;
                invoke-direct {v0}, Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig${'$'}Builder;-><init>()V
                invoke-virtual {v0}, Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig${'$'}Builder;->build()Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig;
                move-result-object v0
                return-object v0
            """,
        )

        // filter injected content from API calls out of lists
        arrayOf(screenMapperFingerprint, nextPageRepositoryImplFingerprint).forEach {
            val startIndex = it.patternMatch!!.startIndex
            it.method.apply {
                val moveInstruction = getInstruction<Instruction11x>(startIndex)

                val listRegister = moveInstruction.registerA

                addInstructions(
                    startIndex + 1,
                    """
                        invoke-static {v$listRegister}, Lapp/revanced/extension/nunl/ScreenMapperPatch;->filterAds(Ljava/util/List;)V
                    """,
                )
            }
        }
    }
}
