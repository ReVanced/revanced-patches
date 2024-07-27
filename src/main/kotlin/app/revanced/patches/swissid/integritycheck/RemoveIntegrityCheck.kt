package app.revanced.patches.swissid.integritycheck

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.swissid.integritycheck.fingerprints.IntegrityCheckFingerprint
import app.revanced.util.exception

@Patch(
    name = "Remove device integrity check",
    description = "Removes the Google Play Device Integrity Check." +
        "With this it's possible to use SwissId on custom ROMS." +
        "Rooted devices should hide the root from SwissId by putting SwissId on the DenyList",
    compatiblePackages = [CompatiblePackage("com.swisssign.swissid.mobile", ["5.2.5"])],
)
@Suppress("unused")
object RemoveIntegrityCheck : BytecodePatch(
    setOf(IntegrityCheckFingerprint),
) {
    override fun execute(context: BytecodeContext) =
        IntegrityCheckFingerprint.result?.mutableMethod?.addInstructions(
            0,
            "iget-object p1, p0, Lcom/swisssign/deviceintegrity/DeviceintegrityPlugin\$onMethodCall\$1;->\$result:Lio/flutter/plugin/common/MethodChannel\$Result;"
            +"\n"
            +"const-string v0, \"VALID\""
            +"\n"
            +"invoke-interface {p1, v0}, Lio/flutter/plugin/common/MethodChannel\$Result;->success(Ljava/lang/Object;)V"
            +"\n"
            +"return-void",
        ) ?: throw IntegrityCheckFingerprint.exception
}
