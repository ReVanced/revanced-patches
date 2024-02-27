package app.revanced.patches.music.utils.microg

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.fix.clientspoof.ClientSpoofPatch
import app.revanced.patches.music.utils.fix.fileprovider.FileProviderPatch
import app.revanced.patches.music.utils.mainactivity.MainActivityResolvePatch
import app.revanced.patches.music.utils.mainactivity.MainActivityResolvePatch.injectInit
import app.revanced.patches.music.utils.microg.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.utils.microg.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.music.utils.microg.fingerprints.CastContextFetchFingerprint
import app.revanced.patches.music.utils.microg.fingerprints.CastDynamiteModuleFingerprint
import app.revanced.patches.music.utils.microg.fingerprints.CastDynamiteModuleV2Fingerprint
import app.revanced.patches.music.utils.microg.fingerprints.GooglePlayUtilityFingerprint
import app.revanced.patches.music.utils.microg.fingerprints.PrimeFingerprint
import app.revanced.patches.music.utils.microg.fingerprints.ServiceCheckFingerprint
import app.revanced.patches.shared.patch.microg.MicroGBytecodeHelper
import app.revanced.patches.shared.patch.packagename.PackageNamePatch

@Patch(
    name = "MicroG support",
    description = "Allows YouTube Music to run without root and under a different package name with MicroG.",
    dependencies = [
        ClientSpoofPatch::class,
        MainActivityResolvePatch::class,
        MicroGResourcePatch::class,
        PackageNamePatch::class,
        FileProviderPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object MicroGPatch : BytecodePatch(
    setOf(
        CastContextFetchFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        GooglePlayUtilityFingerprint,
        PrimeFingerprint,
        ServiceCheckFingerprint
    )
) {
    // NOTE: the previous patch also replaced the following strings, but it seems like they are not needed:
    // - "com.google.android.gms.chimera.GmsIntentOperationService",
    // - "com.google.android.gms.phenotype.internal.IPhenotypeCallbacks",
    // - "com.google.android.gms.phenotype.internal.IPhenotypeService",
    // - "com.google.android.gms.phenotype.PACKAGE_NAME",
    // - "com.google.android.gms.phenotype.UPDATE",
    // - "com.google.android.gms.phenotype",
    override fun execute(context: BytecodeContext) {
        val youtubePackageName = PackageNamePatch.PackageNameYouTube
            ?: throw PatchException("Invalid package name.")

        val musicPackageName = PackageNamePatch.PackageNameYouTubeMusic
            ?: throw PatchException("Invalid package name.")

        if (youtubePackageName == YOUTUBE_PACKAGE_NAME || musicPackageName == MUSIC_PACKAGE_NAME)
            throw PatchException("Original package name is not available as package name for MicroG build.")

        // apply common microG patch
        MicroGBytecodeHelper.patchBytecode(
            context,
            arrayOf(
                MicroGBytecodeHelper.packageNameTransform(
                    YOUTUBE_PACKAGE_NAME,
                    youtubePackageName
                )
            ),
            MicroGBytecodeHelper.PrimeMethodTransformationData(
                PrimeFingerprint,
                MUSIC_PACKAGE_NAME,
                musicPackageName
            ),
            listOf(
                ServiceCheckFingerprint,
                GooglePlayUtilityFingerprint,
                CastDynamiteModuleFingerprint,
                CastDynamiteModuleV2Fingerprint,
                CastContextFetchFingerprint
            )
        )

        injectInit("MicroGPatch", "checkAvailability")

    }
}
