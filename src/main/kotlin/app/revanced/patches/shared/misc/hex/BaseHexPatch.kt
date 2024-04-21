package app.revanced.patches.shared.misc.hex

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.RawResourcePatch
import kotlin.math.max

abstract class BaseHexPatch : RawResourcePatch() {
    internal abstract val replacements: List<Replacement>

    override fun execute(context: ResourceContext) {
        replacements.groupBy { it.targetFilePath }.forEach { (targetFilePath, replacements) ->
            val targetFile = try {
                context[targetFilePath, true]
            } catch (e: Exception) {
                throw PatchException("Could not find target file: $targetFilePath")
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

    /**
     * Represents a pattern to search for and its replacement pattern.
     *
     * @property pattern The pattern to search for.
     * @property replacementPattern The pattern to replace the [pattern] with.
     * @property targetFilePath The path to the file to make the changes in relative to the APK root.
     */
    class Replacement(
        private val pattern: String,
        replacementPattern: String,
        internal val targetFilePath: String,
    ) {
        private val patternBytes = pattern.toByteArrayPattern()
        private val replacementPattern = replacementPattern.toByteArrayPattern()

        init {
            if (this.patternBytes.size != this.replacementPattern.size) {
                throw PatchException("Pattern and replacement pattern must have the same length: $pattern")
            }
        }

        /**
         * Replaces the [patternBytes] with the [replacementPattern] in the [targetFileBytes].
         *
         * @param targetFileBytes The bytes of the file to make the changes in.
         */
        fun replacePattern(targetFileBytes: ByteArray) {
            val startIndex = indexOfPatternIn(targetFileBytes)

            if (startIndex == -1) {
                throw PatchException("Pattern not found in target file: $pattern")
            }

            replacementPattern.copyInto(targetFileBytes, startIndex)
        }

        // TODO: Allow searching in a file channel instead of a byte array to reduce memory usage.
        /**
         * Returns the index of the first occurrence of [patternBytes] in the haystack
         * using the Boyer-Moore algorithm.
         *
         * @param haystack The array to search in.
         *
         * @return The index of the first occurrence of the [patternBytes] in the haystack or -1
         * if the [patternBytes] is not found.
         */
        private fun indexOfPatternIn(haystack: ByteArray): Int {
            val needle = patternBytes

            val haystackLength = haystack.size - 1
            val needleLength = needle.size - 1
            val right = IntArray(256) { -1 }

            for (i in 0 until needleLength) right[needle[i].toInt().and(0xFF)] = i

            var skip: Int
            for (i in 0..haystackLength - needleLength) {
                skip = 0

                for (j in needleLength - 1 downTo 0)
                    if (needle[j] != haystack[i + j]) {
                        skip = max(1, j - right[haystack[i + j].toInt().and(0xFF)])

                        break
                    }

                if (skip == 0) return i
            }
            return -1
        }

        companion object {
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
                    "Could not parse pattern: $this.  A pattern is a sequence of case insensitive strings " +
                        "representing hexadecimal bytes separated by spaces",
                    e,
                )
            }
        }
    }
}
