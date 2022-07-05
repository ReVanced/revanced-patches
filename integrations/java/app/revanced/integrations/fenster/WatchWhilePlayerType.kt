package app.revanced.integrations.fenster

/**
 * WatchWhile player types
 */
@Suppress("unused")
enum class WatchWhilePlayerType {
    NONE,
    HIDDEN,
    WATCH_WHILE_MINIMIZED,
    WATCH_WHILE_MAXIMIZED,
    WATCH_WHILE_FULLSCREEN,
    WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN,
    WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED,
    WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED,
    WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED,
    INLINE_MINIMAL,
    VIRTUAL_REALITY_FULLSCREEN,
    WATCH_WHILE_PICTURE_IN_PICTURE;

    companion object {
        @JvmStatic
        fun safeParseFromString(name: String): WatchWhilePlayerType? {
            return values().firstOrNull { it.name == name }
        }
    }
}