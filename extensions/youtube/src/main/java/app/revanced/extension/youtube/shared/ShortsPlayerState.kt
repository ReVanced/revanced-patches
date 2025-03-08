package app.revanced.extension.youtube.shared

import app.revanced.extension.shared.Logger
import app.revanced.extension.youtube.Event

/**
 * Shorts player state.
 */
class ShortsPlayerState {
    companion object {

        @JvmStatic
        fun setOpen(open: Boolean) {
            if (isOpen != open) {
                Logger.printDebug { "ShortsPlayerState open changed to: $isOpen" }
                isOpen = open
                onChange(open)
            }
        }

        @Volatile
        private var isOpen = false

        /**
         * Shorts player state change listener.
         */
        @JvmStatic
        val onChange = Event<Boolean>()

        /**
         * If the Shorts player is currently open.
         */
        @JvmStatic
        fun isOpen(): Boolean {
            return isOpen
        }
    }
}