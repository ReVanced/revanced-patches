package app.revanced.patches.tiktok.interaction.downloads.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.tiktok.interaction.downloads.annotations.DownloadsCompatibility
import app.revanced.patches.tiktok.interaction.downloads.fingerprints.*

@Patch
@Name("tiktok-download")
@Description("Remove restrictions on downloads video.")
@DownloadsCompatibility
@Version("0.0.1")
class DownloadsPatch : BytecodePatch(
    listOf(
        ACLCommonShareFingerprint,
        ACLCommonShareFingerprint2,
        ACLCommonShareFingerprint3
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val method1 = ACLCommonShareFingerprint.result!!.mutableMethod
        method1.replaceInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )
        val method2 = ACLCommonShareFingerprint2.result!!.mutableMethod
        method2.replaceInstructions(
            0,
            """
                const/4 v0, 0x2
                return v0
            """
        )
        //Download videos without watermark.
        val method3 = ACLCommonShareFingerprint3.result!!.mutableMethod
        method3.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
        return PatchResultSuccess()
    }

}