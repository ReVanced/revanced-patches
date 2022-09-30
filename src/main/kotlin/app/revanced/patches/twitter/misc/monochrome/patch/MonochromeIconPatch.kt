package app.revanced.patches.twitter.misc.monochrome.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.twitter.misc.monochrome.annotations.MonochromeIconCompatibility
import java.io.FileWriter
import java.nio.file.Files

@Patch
@Name("monochrome-icon")
@Description("Adds a monochrome icon.")
@MonochromeIconCompatibility
@Version("0.0.1")
class MonochromeIconPatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val resDirectory = data["res"]
        if (!resDirectory.isDirectory) return PatchResultError("The res folder can not be found.")

        val mipmapV33Directory = resDirectory.resolve("mipmap-anydpi-v33")
        if (!mipmapV33Directory.isDirectory) Files.createDirectories(mipmapV33Directory.toPath())

        FileWriter(
            mipmapV33Directory.resolve("ic_launcher_twitter.xml"),
        ).use {
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<adaptive-icon\n" +
                    "  xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                    "    <background android:drawable=\"@color/ic_launcher_background\" />\n" +
                    "    <foreground android:drawable=\"@mipmap/ic_launcher_twitter_foreground\" />\n" +
                    "    <monochrome android:drawable=\"@mipmap/ic_launcher_twitter_foreground\" />\n" +
                    "</adaptive-icon>"
        }

        FileWriter(
            mipmapV33Directory.resolve("ic_launcher_twitter_round.xml"),
        ).use {
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<adaptive-icon\n" +
                    "  xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                    "    <background android:drawable=\"@color/ic_launcher_background\" />\n" +
                    "    <foreground android:drawable=\"@mipmap/ic_launcher_twitter_foreground\" />\n" +
                    "    <monochrome android:drawable=\"@mipmap/ic_launcher_twitter_foreground\" />\n" +
                    "</adaptive-icon>"
        }

        return PatchResultSuccess()
    }
}