package app.revanced.patches.shared.misc.hex

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.rawResourcePatch
import kotlin.math.max

// The replacements being passed using a function is intended.
// Previously the replacements were a property of the patch. Getter were being delegated to that property.
// This late evaluation was being leveraged in app.revanced.patches.all.misc.hex.HexPatch.
// Without the function, the replacements would be evaluated at the time of patch creation.
// This isn't possible because the delegated property is not accessible at that time.
fun hexPatch(ignoreMissingTargetFiles: Boolean = false, replacementsSupplier: () -> Set<Replacement>) = rawResourcePatch {
    execute {
        replacementsSupplier().groupBy { it.targetFilePath }.forEach { (targetFilePath, replacements) ->
            val targetFile = try {
                get(targetFilePath, true)
            } catch (_: Exception) {
                if (ignoreMissingTargetFiles) return@forEach
                throw PatchException("Could not find required target file: $targetFilePath")
            }

            // TODO: Use a file channel to read and write the file instead of reading the whole file into memory,
            //  in order to reduce memory usage.
            val targetFileBytes = targetFile.readBytes()

            replacements.forEach { replacement ->
                replacement.replacePattern(targetFileBytes)
            }

            targetFile.writeBytes(targetFileBytes)
        }
    }
}

/**
 * Represents a pattern to search for and its replacement pattern in a file.
 *
 * @property bytes The bytes to search for.
 * @property replacementBytes The bytes to replace the [bytes] with.
 * @property targetFilePath The path to the file to make the changes in relative to the APK root.
 */
class Replacement(
    private val bytes: ByteArray,
    private val replacementBytes: ByteArray,
    internal val targetFilePath: String,
) {
    /**
     * Represents a pattern to search for and its replacement pattern.
     *
     * @param pattern The pattern to search for.
     * @param replacementPattern The pattern to replace the [pattern] with.
     * @param targetFilePath The path to the file to make the changes in relative to the APK root.
     */
    constructor(
        pattern: String,
        replacementPattern: String,
        targetFilePath: String,
    ) : this(
        pattern.toByteArrayPattern(),
        replacementPattern.toByteArrayPattern(), targetFilePath
    )

    init {
        if (this.bytes.size != this.replacementBytes.size) {
            throw PatchException(
                "Pattern and replacement pattern must have the same length: ${bytes.toStringPattern()}"
            )
        }
    }

    /**
     * Replaces the [bytes] with the [replacementBytes] in the [targetFileBytes].
     *
     * @param targetFileBytes The bytes of the file to make the changes in.
     */
    fun replacePattern(targetFileBytes: ByteArray) {
        val startIndex = indexOfPatternIn(targetFileBytes)

        if (startIndex == -1) {
            throw PatchException("Pattern not found in target file: ${bytes.toStringPattern()}")
        }

        replacementBytes.copyInto(targetFileBytes, startIndex)
    }

    // TODO: Allow searching in a file channel instead of a byte array to reduce memory usage.
    /**
     * Returns the index of the first occurrence of [bytes] in the haystack
     * using the Boyer-Moore algorithm.
     *
     * @param haystack The array to search in.
     *
     * @return The index of the first occurrence of the [bytes] in the haystack or -1
     * if the [bytes] is not found.
     */
    private fun indexOfPatternIn(haystack: ByteArray): Int {
        val needle = bytes

        val haystackLength = haystack.size - 1
        val needleLength = needle.size - 1
        val right = IntArray(256) { -1 }

        for (i in 0 until needleLength) right[needle[i].toInt().and(0xFF)] = i

        var skip: Int
        for (i in 0..haystackLength - needleLength) {
            skip = 0

            for (j in needleLength - 1 downTo 0) {
                if (needle[j] != haystack[i + j]) {
                    skip = max(1, j - right[haystack[i + j].toInt().and(0xFF)])

                    break
                }
            }

            if (skip == 0) return i
        }
        return -1
    }

    companion object {
        /**
         * Creates a [Replacement] instance from a string representation of a pattern and its replacement.
         */
        fun replacementOf(
            fromString: String,
            toString: String,
            forFile: String
        ): Replacement {
            val firstBytes = fromString.toByteArray()
            val secondBytes = toString.toByteArray()

            val secondBytesString = secondBytes + ByteArray(firstBytes.size - secondBytes.size)


            return Replacement(
                firstBytes.toStringPattern(),
                secondBytesString.toStringPattern(),
                forFile
            )
        }

        private fun ByteArray.toStringPattern() = joinToString(" ") { "%02x".format(it) }
    
        /**
         * Convert a string representing a pattern of hexadecimal bytes to a byte array.
         *
         * @return The byte array representing the pattern.
         * @throws PatchException If the pattern is invalid.
         */
        private fun String.toByteArrayPattern() = try {
            split(" ").map { it.toInt(16).toByte() }.toByteArray()
        } catch (e: NumberFormatException) {
            throw PatchException(
                "Could not parse pattern: $this. A pattern is a sequence of case insensitive strings " +
                        "representing hexadecimal bytes separated by spaces",
                e,
            )
        }
    }
}
