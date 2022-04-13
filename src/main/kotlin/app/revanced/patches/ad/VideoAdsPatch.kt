package app.revanced.patches.ad

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.extensions.AccessFlagExtensions.Companion.or
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.*
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.smali.asInstructions
import app.revanced.patches.SHARED_METADATA
import org.jf.dexlib2.AccessFlags

class VideoAdsPatch : Patch(
    PatchMetadata(
        "video-ads",
        "TODO",
        "TODO"
    )
) {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["show-video-ads-constructor"].findParentMethod(
            MethodSignature(
                "show-video-ads-method",
                SHARED_METADATA,
                "V",
                AccessFlags.PUBLIC or AccessFlags.FINAL,
                listOf("Z"),
                null
            )
        ) ?: return PatchResultError("Could not find required method to patch")

        // Override the parameter by calling shouldShowAds and setting the parameter to the result
        map.method.implementation!!.addInstructions(
            0,
            """
                invoke-static { }, Lfi/vanced/libraries/youtube/whitelisting/Whitelist;->shouldShowAds()Z
                move-result v1
            """.trimIndent().asInstructions()
        )

        return PatchResultSuccess()
    }
}

