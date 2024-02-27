package app.revanced.patches.shared.patch.packagename

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption

@Patch(
    name = "Custom package name",
    description = "Changes the package name for the non-root build of YouTube and YouTube Music to the name specified in options.json.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube"),
        CompatiblePackage("com.google.android.apps.youtube.music")
    ]
)
@Suppress("unused")
object PackageNamePatch : ResourcePatch() {
    private const val CLONE_PACKAGE_NAME_YOUTUBE = "com.rvx.android.youtube"
    private const val DEFAULT_PACKAGE_NAME_YOUTUBE = "app.rvx.android.youtube"

    private const val CLONE_PACKAGE_NAME_YOUTUBE_MUSIC = "com.rvx.android.apps.youtube.music"
    private const val DEFAULT_PACKAGE_NAME_YOUTUBE_MUSIC = "app.rvx.android.apps.youtube.music"

    internal val PackageNameYouTube by stringPatchOption(
        key = "PackageNameYouTube",
        default = DEFAULT_PACKAGE_NAME_YOUTUBE,
        values = mapOf(
            "Clone" to CLONE_PACKAGE_NAME_YOUTUBE,
            "Default" to DEFAULT_PACKAGE_NAME_YOUTUBE
        ),
        title = "Package name of YouTube",
        description = "The name of the package to use in MicroG support",
        required = true
    )

    internal val PackageNameYouTubeMusic by stringPatchOption(
        key = "PackageNameYouTubeMusic",
        default = DEFAULT_PACKAGE_NAME_YOUTUBE_MUSIC,
        values = mapOf(
            "Clone" to CLONE_PACKAGE_NAME_YOUTUBE_MUSIC,
            "Default" to DEFAULT_PACKAGE_NAME_YOUTUBE_MUSIC
        ),
        title = "Package name of YouTube Music",
        description = "The name of the package to use in MicroG support",
        required = true
    )

    override fun execute(context: ResourceContext) {
    }
}
