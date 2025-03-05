package app.revanced.extension.youtube.shared

import app.revanced.extension.shared.Logger
import app.revanced.extension.youtube.Event

/**
 * Shorts player state.
 */
enum class ShortsPlayerState {
    CLOSED,
    OPEN;

    companion object {

        @JvmStatic
        fun set(enum: ShortsPlayerState) {
            if (currentShortsPlayerState != enum) {
                Logger.printDebug { "ShortsPlayerState changed to: ${enum.name}" }
                currentShortsPlayerState = enum
                onChange(enum)
            }
        }

        @Volatile // Read/write from different threads.
        private var currentShortsPlayerState = CLOSED

        /**
         * Shorts player state change listener.
         */
        @JvmStatic
        val onChange = Event<ShortsPlayerState>()

        /**
         * If the Shorts player is currently open.
         */
        @JvmStatic
        fun isOpen(): Boolean {
            return currentShortsPlayerState == OPEN
        }
    }
}