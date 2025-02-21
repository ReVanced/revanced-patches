package app.revanced.patches.nunl.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hide ads and sponsored articles in list pages and remove pre-roll ads on videos.",
) {
    compatibleWith("nl.sanomamedia.android.nu"("11.0.0", "11.0.1"))

    dependsOn(sharedExtensionPatch("nunl"))

    execute {
        // Disable video pre-roll ads.
        // Whenever the app tries to create an ad via JWUtils.createAdvertising, don't actually tell the underlying JWPlayer library to do so => JWPlayer will not display ads
        jwUtilCreateAdvertisementFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig${'$'}Builder;
                invoke-direct { v0 }, Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig${'$'}Builder;-><init>()V
                invoke-virtual { v0 }, Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig${'$'}Builder;->build()Lcom/jwplayer/pub/api/configuration/ads/VastAdvertisingConfig;
                move-result-object v0
                return-object v0
            """,
        )

        // Filter injected content from API calls out of lists.
        arrayOf(screenMapperFingerprint, nextPageRepositoryImplFingerprint).forEach {
            // index of instruction moving result of BlockPage;->getBlocks(...)
            val moveGetBlocksResultObjectIndex = it.patternMatch!!.startIndex
            it.method.apply {
                val moveInstruction = getInstruction<OneRegisterInstruction>(moveGetBlocksResultObjectIndex)

                val listRegister = moveInstruction.registerA

                // add instruction after moving List<Block> to register and then filter this List<Block> in place
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
