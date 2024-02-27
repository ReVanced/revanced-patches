package app.revanced.patches.music.layout.overlayfilter

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch

@Patch(
    name = "Disable overlay filter",
    description = "Removes the dark overlay when comment, share, save to playlist, and flyout panels are open.",
    dependencies = [OverlayFilterBytecodePatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
    use = false
)
@Suppress("unused")
object OverlayFilterPatch : ResourcePatch() {

    override fun execute(context: ResourceContext) {
        val styleFile = context["res/values/styles.xml"]

        styleFile.writeText(
            styleFile.readText()
                .replace(
                    "ytOverlayBackgroundMedium\">@color/yt_black_pure_opacity60",
                    "ytOverlayBackgroundMedium\">@android:color/transparent"
                )
        )
    }
}