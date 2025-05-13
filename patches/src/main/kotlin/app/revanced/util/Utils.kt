package app.revanced.util

internal object Utils {
    internal fun String.trimIndentMultiline() =
        this.split("\n")
            .joinToString("\n") { it.trimIndent() } // Remove the leading whitespace from each line.
            .trimIndent() // Remove the leading newline.
}

internal fun formatAsHex(value: String): String = "0x${value.uppercase()}"

internal fun Boolean.toHexString(): String = if (this) "0x1" else "0x0"

internal fun Byte.toHexString(): String = formatAsHex(this.toUByte().toString(16))

internal fun Short.toHexString(): String = formatAsHex(this.toUShort().toString(16))

internal fun Int.toHexString(): String = formatAsHex(this.toUInt().toString(16))

internal fun Long.toHexString(): String = formatAsHex(this.toULong().toString(16))

internal fun Float.toHexString(): String = this.toRawBits().toHexString()

internal fun Double.toHexString(): String = this.toRawBits().toHexString()

internal fun Char.toHexString(): String = this.code.toHexString()