package app.revanced.patches.swissid.integritycheck

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.swissid.integritycheck.fingerprints.CheckIntegrityFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Remove Google Play Integrity check",
    description = "Removes the Google Play Integrity check. With this it's possible to use SwissID on custom ROMS." +
            "If the device is rooted, root permissions must be hidden from the app.",
    compatiblePackages = [CompatiblePackage("com.swisssign.swissid.mobile")],
)
@Suppress("unused")
object RemoveGooglePlayIntegrityCheck : BytecodePatch(
    setOf(CheckIntegrityFingerprint),
) {
    private const val RESULT_METHOD_REFERENCE = " Lcom/swisssign/deviceintegrity/" +
            "DeviceintegrityPlugin\$onMethodCall\$1;->\$result:" +
            "Lio/flutter/plugin/common/MethodChannel\$Result;"
    private const val SUCCESS_METHOD_REFERENCE =
        "Lio/flutter/plugin/common/MethodChannel\$Result;->success(Ljava/lang/Object;)V"

    override fun execute(context: BytecodeContext) =
        CheckIntegrityFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                iget-object p1, p0, $RESULT_METHOD_REFERENCE
                const-string v0, "VALID"
                invoke-interface {p1, v0}, $SUCCESS_METHOD_REFERENCE
                return-void
            """,
        )
}
