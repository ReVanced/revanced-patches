package app.revanced.patches.strava.media.upload

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.longOption
import app.revanced.util.returnEarly

@Suppress("unused")
val overwriteMediaUploadParametersPatch = bytecodePatch(
    name = "Overwrite media upload parameters",
    description = "Overwrites the compression, resize and trim media (images and videos) parameters returned by Strava's server before upload.",
) {
    compatibleWith("com.strava")

    val compressionQuality by intOption(
        key = "compressionQuality",
        title = "Compression quality (percent)",
        description = "This is used as the JPEG quality setting (≤ 100).",
    ) { it == null || it in 1..100 }

    val maxDuration by longOption(
        key = "maxDuration",
        title = "Max duration (seconds)",
        description = "The maximum length (≤ ${60 * 60}) of a video before it gets trimmed.",
    ) { it == null || it in 1..60 * 60 }

    val maxSize by intOption(
        key = "maxSize",
        title = "Max size (pixels)",
        description = "The image gets resized so that the smaller dimension (width/height) does not exceed this value (≤ 10000).",
    ) { it == null || it in 1..10000 }

    execute {
        val mediaUploadParametersClass = classes.single { it.endsWith("/MediaUploadParameters;") }

        compressionQuality?.let { compressionQuality ->
            getCompressionQualityFingerprint.match(mediaUploadParametersClass).method.returnEarly(compressionQuality / 100f)
        }

        maxDuration?.let { maxDuration ->
            getMaxDurationFingerprint.match(mediaUploadParametersClass).method.returnEarly(maxDuration)
        }

        maxSize?.let {
            getMaxSizeFingerprint.match(mediaUploadParametersClass).method.returnEarly(it)
        }
    }
}
