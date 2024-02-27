package app.revanced.patches.music.utils.fix.fileprovider

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.music.utils.fix.fileprovider.fingerprints.FileProviderResolverFingerprint
import app.revanced.patches.shared.patch.packagename.PackageNamePatch
import app.revanced.util.exception

@Patch(dependencies = [PackageNamePatch::class])
object FileProviderPatch : BytecodePatch(
    setOf(FileProviderResolverFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        val youtubePackageName = PackageNamePatch.PackageNameYouTube
            ?: throw PatchException("Invalid package name.")

        val musicPackageName = PackageNamePatch.PackageNameYouTubeMusic
            ?: throw PatchException("Invalid package name.")

        /**
         * For some reason, if the app gets "android.support.FILE_PROVIDER_PATHS",
         * the package name of YouTube is used, not the package name of the YT Music.
         *
         * There is no issue in the stock YT Music, but this is an issue in the MicroG Build.
         * https://github.com/inotia00/ReVanced_Extended/issues/1830
         *
         * To solve this issue, replace the package name of YouTube with YT Music's package name.
         */
        FileProviderResolverFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    0, """
                        const-string v0, "$youtubePackageName.fileprovider"
                        invoke-static {p1, v0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
                        move-result v0
                        if-eqz v0, :ignore
                        const-string p1, "$musicPackageName.fileprovider"
                        """, ExternalLabel("ignore", getInstruction(0))
                )
            }
        } ?: throw FileProviderResolverFingerprint.exception

    }
}
