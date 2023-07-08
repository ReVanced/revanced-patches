package app.revanced.integrations.shared

import app.revanced.integrations.utils.Event
import app.revanced.integrations.utils.LogHelper

/**
 * WatchWhile player type
 */
enum class PlayerType {
    /**
     * Either no video, or a Short is playing.
     */
    NONE,
    /**
     * A Short is playing. Occurs if a regular video is first opened
     * and then a Short is opened (without first closing the regular video).
     */
    HIDDEN,
    /**
     * When spoofing to 16.x YouTube and watching a short with a regular video in the background,
     * the type will be this (and not [HIDDEN]).
     */
    WATCH_WHILE_MINIMIZED,
    WATCH_WHILE_MAXIMIZED,
    WATCH_WHILE_FULLSCREEN,
    WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN,
    WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED,
    /**
     * When opening a short while a regular video is minimized, the type can momentarily be this.
     */
    WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED,
    WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED,
    /**
     * Home feed video playback.
     */
    INLINE_MINIMAL,
    VIRTUAL_REALITY_FULLSCREEN,
    WATCH_WHILE_PICTURE_IN_PICTURE;

    companion object {

        private val nameToPlayerType = values().associateBy { it.name }

        @JvmStatic
        fun setFromString(enumName: String) {
            val newType = nameToPlayerType[enumName]
            if (newType == null) {
                LogHelper.printException { "Unknown PlayerType encountered: $enumName" }
            } else if (current != newType) {
                LogHelper.printDebug { "PlayerType changed to: $newType" }
                current = newType
            }
        }

        /**
         * The current player type.
         */
        @JvmStatic
        var current
            get() = currentPlayerType
            private set(value) {
                currentPlayerType = value
                onChange(currentPlayerType)
            }
        @Volatile // value is read/write from different threads
        private var currentPlayerType = NONE

        /**
         * player type change listener
         */
        @JvmStatic
        val onChange = Event<PlayerType>()
    }

    /**
     * Check if the current player type is [NONE] or [HIDDEN].
     * Useful to check if a short is currently playing.
     *
     * Does not include the first moment after a short is opened when a regular video is minimized on screen,
     * or while watching a short with a regular video present on a spoofed 16.x version of YouTube.
     * To include those situations instead use [isNoneHiddenOrMinimized].
     */
    fun isNoneOrHidden(): Boolean {
        return this == NONE || this == HIDDEN
    }

    /**
     * Check if the current player type is
     * [NONE], [HIDDEN], [WATCH_WHILE_MINIMIZED], [WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED].
     *
     * Useful to check if a Short is being played,
     * although will return false positive if a regular video is opened and minimized (and no short is playing).
     *
     * @return If nothing, a Short,
     *         or a regular video is minimized video or sliding off screen to a dismissed or hidden state.
     */
    fun isNoneHiddenOrMinimized(): Boolean {
        return this == NONE || this == HIDDEN
                || this == WATCH_WHILE_MINIMIZED
                || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED
    }

}