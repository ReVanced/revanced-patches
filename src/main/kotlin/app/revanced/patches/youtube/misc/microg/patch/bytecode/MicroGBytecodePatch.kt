package app.revanced.patches.youtube.misc.microg.patch.bytecode

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.fingerprints.WatchWhileActivityFingerprint
import app.revanced.patches.shared.misc.fix.spoof.patch.ClientSpoofPatch
import app.revanced.patches.youtube.layout.buttons.cast.patch.HideCastButtonPatch
import app.revanced.patches.youtube.misc.fix.playback.patch.SpoofSignatureVerificationPatch
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility
import app.revanced.patches.youtube.misc.microg.fingerprints.*
import app.revanced.patches.youtube.misc.microg.patch.resource.MicroGResourcePatch
import app.revanced.patches.youtube.misc.microg.shared.Constants.PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_PACKAGE_NAME
import app.revanced.util.microg.MicroGBytecodeHelper

@Patch
@DependsOn(
    [
        MicroGResourcePatch::class,
        HideCastButtonPatch::class,
        ClientSpoofPatch::class
    ]
)
@Name("microg-support")
@Description("Allows YouTube ReVanced to run without root and under a different package name with Vanced MicroG.")
@MicroGPatchCompatibility
@Version("0.0.1")
class MicroGBytecodePatch : BytecodePatch(
    listOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
        PrimeFingerprint,
        WatchWhileActivityFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // apply common microG patch
        MicroGBytecodeHelper.patchBytecode(
            context, arrayOf(
                MicroGBytecodeHelper.packageNameTransform(
                    PACKAGE_NAME,
                    REVANCED_PACKAGE_NAME
                )
            ),
            MicroGBytecodeHelper.PrimeMethodTransformationData(
                PrimeFingerprint,
                PACKAGE_NAME,
                REVANCED_PACKAGE_NAME
            ),
            listOf(
                ServiceCheckFingerprint,
                GooglePlayUtilityFingerprint,
                CastDynamiteModuleFingerprint,
                CastDynamiteModuleV2Fingerprint,
                CastContextFetchFingerprint
            )
        )

        // inject the notice for MicroG
        MicroGBytecodeHelper.injectNotice(WatchWhileActivityFingerprint)

        return PatchResultSuccess()
    }
}
