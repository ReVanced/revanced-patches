package app.revanced.patches.tumblr.fixes

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.fixes.fingerprints.HttpPathParserFingerprint
import app.revanced.patches.tumblr.fixes.fingerprints.AddQueryParamFingerprint
import app.revanced.util.exception

@Patch(
    name = "Fix old versions",
    description = "Fixes old versions of the app (v33.2 and earlier) breaking due to Tumblr removing remnants of Tumblr" +
            " Live from the API, which causes many requests to fail. This patch has no effect on newer versions of the app.",
    compatiblePackages = [CompatiblePackage("com.tumblr")],
    use = false,
)
@Suppress("unused")
object FixOldVersionsPatch : BytecodePatch(
    setOf(HttpPathParserFingerprint, AddQueryParamFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        val blockedStrings = listOf(
            ",?live_now", ",?live_streaming_user_id"
        )

        HttpPathParserFingerprint.result?.let {
            val endIndex = it.scanResult.patternScanResult!!.endIndex
            // Remove each of the blockedStrings elements from statically defined request URLs
            for (blockedString in blockedStrings) {
                it.mutableMethod.addInstructions(
                    endIndex + 1,
                    """
                    # Remove blocked string from URL path (p2)
                    # path = path.replace(blockedString, "")
                    const-string p1, "$blockedString"
                    const-string p3, ""
                    invoke-virtual {p2, p1, p3}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                    move-result-object p2
                """
                )
            }
        } ?: throw HttpPathParserFingerprint.exception

        AddQueryParamFingerprint.result?.let {
            // Remove each of the blockedStrings elements when passed in as a @Query argument
            for (blockedString in blockedStrings) {
                it.mutableMethod.addInstructions(
                    0,
                    """
                    # Remove blocked string from query parameter value (p2)
                    # value = value.replace(blockedString, "")
                    const-string v0, "$blockedString"
                    const-string v1, ""
                    invoke-virtual {p2, v0, v1}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                    move-result-object p2
                """
                )
            }
        } ?: throw AddQueryParamFingerprint.exception
    }
}
