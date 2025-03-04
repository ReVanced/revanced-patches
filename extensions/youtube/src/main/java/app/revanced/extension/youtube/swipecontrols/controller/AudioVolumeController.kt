package app.revanced.extension.youtube.swipecontrols.controller

import android.content.Context
import android.media.AudioManager
import android.os.Build
import app.revanced.extension.shared.Logger.printException
import app.revanced.extension.youtube.swipecontrols.misc.clamp
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * controller to adjust the device volume level
 *
 * @param context the context to bind the audio service in
 * @param maxVolumeConfig number of volume steps set by user in settings
 * @param targetStream the stream that is being controlled. Must be one of the STREAM_* constants in [AudioManager]
 */
class AudioVolumeController(
    context: Context,
    maxVolumeConfig: Int,
    private val targetStream: Int = AudioManager.STREAM_MUSIC,
) {

    /**
     * audio service connection
     */
    private lateinit var audioManager: AudioManager
    private var minimumVolumeIndex by Delegates.notNull<Int>()
    private var maximumVolumeIndex by Delegates.notNull<Int>()
    private var ratio by Delegates.notNull<Float>()

    init {
        // bind audio service
        val mgr = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (mgr == null) {
            printException { "failed to acquire AUDIO_SERVICE" }
        } else {
            audioManager = mgr

            val realMax = audioManager.getStreamMaxVolume(targetStream)

            // Value from config can't be higher than real max volume,
            // otherwise it breaks volume up.
            if(maxVolumeConfig <= 0 || maxVolumeConfig >= realMax) {
                maximumVolumeIndex = realMax
                ratio = 1F
            } else {
                maximumVolumeIndex = maxVolumeConfig
                ratio = realMax / maxVolumeConfig.toFloat()
            }

            minimumVolumeIndex =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    audioManager.getStreamMinVolume(
                        targetStream,
                    )
                } else {
                    0
                }
        }
    }

    /**
     * the current volume, ranging from 0.0 to [maxVolume]
     */
    var volume: Int
        get() {
            // check if initialized correctly
            if (!this::audioManager.isInitialized) return 0

            // get current volume
            return currentVolumeIndex - minimumVolumeIndex
        }
        set(value) {
            // check if initialized correctly
            if (!this::audioManager.isInitialized) return

            // set new volume
            currentVolumeIndex =
                (value + minimumVolumeIndex).clamp(minimumVolumeIndex, maximumVolumeIndex)
        }

    /**
     * the maximum possible volume
     */
    val maxVolume: Int
        get() = maximumVolumeIndex - minimumVolumeIndex

    /**
     * the current volume index of the target stream
     */
    private var currentVolumeIndex: Int
        get() = (audioManager.getStreamVolume(targetStream) / ratio).roundToInt()
        set(value) = audioManager.setStreamVolume(targetStream, (value * ratio).roundToInt(), 0)
}
