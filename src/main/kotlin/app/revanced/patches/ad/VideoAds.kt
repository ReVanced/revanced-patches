package app.revanced.patches.ad

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.smali.asInstructions
import org.jf.dexlib2.AccessFlags

class VideoAds : Patch("VideoAds") {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["show-video-ads-constructor"].findParentMethod(
            MethodSignature(
                "show-video-ads-method",
                "V",
                AccessFlags.PUBLIC or AccessFlags.FINAL,
                setOf("Z"),
                null
            )
        ) ?: return PatchResultError("Could not find required method to patch")

        // Override the parameter by calling shouldShowAds and setting the parameter to the result
        map.resolveAndGetMethod().implementation!!.addInstructions(
            0,
            """
                invoke-static { }, Lfi/vanced/libraries/youtube/whitelisting/Whitelist;->shouldShowAds()Z
                move-result v0
            """.trimIndent().asInstructions()
        )

        return PatchResultSuccess()
    }
}

