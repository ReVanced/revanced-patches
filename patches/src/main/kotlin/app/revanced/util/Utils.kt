package app.revanced.util

internal object Utils {
    internal fun String.trimIndentMultiline() =
        this.split("\n")
            .joinToString("\n") { it.trimIndent() } // Remove the leading whitespace from each line.
            .trimIndent() // Remove the leading newline.
}

/**
 * If the current VM environment is an Android device. This will return false
 * if running Termux or any other virtualized environment inside Android.
 */
internal val isAndroidRuntime by lazy {
    try {
        Class.forName("android.os.Build")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
