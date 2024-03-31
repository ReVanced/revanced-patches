package app.revanced.patches.tumblr.fixes

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.fixes.fingerprints.HttpPathParserFingerprint
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
    setOf(HttpPathParserFingerprint),
) {
    override fun execute(context: BytecodeContext) =
        HttpPathParserFingerprint.result?.let {
            val endIndex = it.scanResult.patternScanResult!!.endIndex

            it.mutableMethod.addInstructions(
                endIndex + 1,
                """
                # Remove "?live_now" from the request path p2.
                # p2 = p2.replace(p1, p3)
                const-string p1, ",?live_now"
                const-string p3, ""
                invoke-virtual {p2, p1, p3}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                move-result-object p2
            """,
            )
        } ?: throw HttpPathParserFingerprint.exception
}
