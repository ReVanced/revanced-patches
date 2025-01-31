package app.revanced.patches.swissid.integritycheck

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

private const val RESULT_METHOD_REFERENCE =
    " Lcom/swisssign/deviceintegrity/DeviceintegrityPlugin\$onMethodCall\$1;->" +
        "\$result:Lio/flutter/plugin/common/MethodChannel\$Result;"
private const val SUCCESS_METHOD_REFERENCE =
    "Lio/flutter/plugin/common/MethodChannel\$Result;->success(Ljava/lang/Object;)V"

@Suppress("unused")
val removeGooglePlayIntegrityCheckPatch = bytecodePatch(
    name = "Remove Google Play Integrity check",
    description = "Removes the Google Play Integrity check. With this it's possible to use SwissID on custom ROMS." +
        "If the device is rooted, root permissions must be hidden from the app.",
) {
    compatibleWith("com.swisssign.swissid.mobile"("5.2.9"))

    execute {
        checkIntegrityFingerprint.method.addInstructions(
            0,
            """
                iget-object p1, p0, $RESULT_METHOD_REFERENCE
                const-string v0, "VALID"
                invoke-interface {p1, v0}, $SUCCESS_METHOD_REFERENCE
                return-void
            """,
        )
    }
}
