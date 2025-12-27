package app.revanced.util

internal fun String.trimIndentMultiline() =
    this.split("\n")
        .joinToString("\n") { it.trimIndent() } // Remove the leading whitespace from each line.
        .trimIndent() // Remove the leading newline.

internal fun Boolean.toHexString(): String = if (this) "0x1" else "0x0"

internal val isAndroid = try {
    Class.forName("android.os.Build")
    true
} catch (_: ClassNotFoundException) {
    false
}