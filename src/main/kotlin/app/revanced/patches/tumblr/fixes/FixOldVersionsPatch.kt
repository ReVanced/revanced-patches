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
    description = "Fixes old versions of the app to break due to Tumblr removing remnants of Tumblr Live from the API, " +
        "that cause many requests on old app versions to fail. This patch has no effect on newer versions of the app.",
    compatiblePackages = [CompatiblePackage("com.tumblr")],
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
                # p2 is "value" string which has the value of the annotation (= the request path)
                # p2 = p2.replace(v0, v1)
                const-string p1, ",?live_now"
                const-string p3, ""
                invoke-virtual {p2, p1, p3}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                move-result-object p2
            """,
            )
        } ?: throw HttpPathParserFingerprint.exception
}
