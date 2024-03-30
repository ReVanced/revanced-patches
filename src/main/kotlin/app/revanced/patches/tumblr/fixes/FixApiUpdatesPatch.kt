package app.revanced.patches.tumblr.fixes

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.fixes.fingerprints.HttpPathParserFingerprint
import app.revanced.util.exception

// Tumblr removed remnants of Tumblr Live from the API, which causes many requests on old app versions to fail
// with error HTTP 400: "Unknown attribute 'live_now' requested"
// To fix this, we simply stop the app from requesting this attribute by removing it from the path of API endpoints
// Note: compatiblePackages intentionally doesn't have app versions specified so ReVanced Manager does not show
// the old app version as the "recommended" version. This patch does nothing in newer app versions.
@Patch(
    name = "Fix old apps breaking",
    description = "Fixes old app versions breaking due to Tumblr API updates.",
    compatiblePackages = [CompatiblePackage("com.tumblr")]
)
@Suppress("unused")
object FixApiUpdatesPatch : BytecodePatch(
    setOf(HttpPathParserFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        HttpPathParserFingerprint.result?.let {
            val endIndex = it.scanResult.patternScanResult!!.endIndex

            it.mutableMethod.addInstructions(
                endIndex + 1,
                """
                # Parameter 2 is "value" string which has the value of the annotation (= the request path)
                # We want to replace ",?live_now" with "" in this parameter
                
                # Method parameters P1 and P3 are no longer used at injection point, reuse their registers
                const-string p1, ",?live_now" # Replace FROM
                const-string p3, "" # Replace TO
                
                # String.replace: p2.replace(v0, v1)
                invoke-virtual {p2, p1, p3}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                
                # Store string with ",?live_now" removed back in p2
                move-result-object p2
            """)
        } ?: throw HttpPathParserFingerprint.exception
}