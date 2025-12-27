package app.revanced.patches.strava.mediaupload

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.longOption

@Suppress("unused")
val overwriteMediaUploadParametersPatch = bytecodePatch(
    name = "Overwrite media upload parameters",
    description = "The app compresses, resizes and trims media (images and videos) before upload. " +
            "This patch overwrites the parameter values returned by Strava's server. " +
            "Leave each option empty to accept the defaults (which may change over time).",
) {
    compatibleWith("com.strava")

    val compressionQuality by intOption(
        key = "compressionQuality",
        title = "Compression quality (percent), default: 75",
        description = "This is used as the JPEG quality setting.",
    ) { it == null || it in 1..100 }

    val maxDuration by longOption(
        key = "maxDuration",
        title = "Max duration (seconds), default: 30",
        description = "The maximum length of a video before it gets trimmed.",
    ) { it == null || it in 1..60 * 60 }

    val maxSize by intOption(
        key = "maxSize",
        title = "Max size (pixels), default: 1600",
        description = "The image gets resized so that the smaller dimension (width/height) does not exceed this value.",
    ) { it == null || it in 1..10000 }

    execute {
        val mediaUploadParametersClass = classes.single { it.endsWith("/MediaUploadParameters;") }

        compressionQuality?.let { compressionQuality ->
            val getCompressionQualityIndex = getCompressionQualityFingerprint.match(mediaUploadParametersClass).patternMatch!!.startIndex
            getCompressionQualityFingerprint.method.apply { 
                removeInstruction(getCompressionQualityIndex)
                addInstructions(
                    getCompressionQualityIndex,
                    """
                    const v0, ${compressionQuality / 100f}f
                    invoke-static { v0 }, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;
                    move-result-object v0
                """,
                )
            }
        }

        maxDuration?.let { maxDuration ->
            val getMaxDurationIndex = getMaxDurationFingerprint.match(mediaUploadParametersClass).patternMatch!!.startIndex
            getMaxDurationFingerprint.method.apply {
                removeInstruction(getMaxDurationIndex)
                addInstructions(
                    getMaxDurationIndex,
                    """
                    const-wide v0, ${maxDuration}L
                    invoke-static { v0, v1 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                    move-result-object v0
                """,
                )
            }
        }

        maxSize?.let {
            getMaxSizeFingerprint.match(mediaUploadParametersClass).method.replaceInstruction(
                getMaxSizeFingerprint.patternMatch!!.startIndex,
                "const v0, $it",
            )
        }
    }
}
