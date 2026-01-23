package app.revanced.patches.strava.mediaupload

import app.revanced.patcher.firstClassDef
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.longOption
import app.revanced.util.returnEarly

@Suppress("unused")
val `Overwrite media upload parameters` by creatingBytecodePatch(
    description = "Overwrites the compression, resize and trim media (images and videos) parameters returned by Strava's server before upload.",
) {
    compatibleWith("com.strava")

    val compressionQuality by intOption(
        name = "Compression quality (percent)",
        description = "This is used as the JPEG quality setting (≤ 100).",
    ) { it == null || it in 1..100 }

    val maxDuration by longOption(
        name = "maxDuration",
        description = "The maximum length (≤ ${60 * 60}) of a video before it gets trimmed.",
    ) { it == null || it in 1..60 * 60 }

    val maxSize by intOption(
        name = "Max size (pixels)",
        description = "The image gets resized so that the smaller dimension (width/height) does not exceed this value (≤ 10000).",
    ) { it == null || it in 1..10000 }

    apply {
        val mediaUploadParametersClass = firstClassDef { type.endsWith("/MediaUploadParameters;") }

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
