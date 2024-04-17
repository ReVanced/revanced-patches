package app.revanced.patches.all.misc.hex

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.RawResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.registerNewPatchOption
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.all.misc.hex.HexPatch.Replacement.Companion.replacementOf
import app.revanced.util.Utils.trimIndentMultiline
import kotlin.math.max

@Patch(
    name = "Hex",
    description = "Replaces a hexadecimal patterns of bytes of files in an APK.",
    use = false,
)
@Suppress("unused")
object HexPatch : RawResourcePatch() {
    private val targetFilePath by stringPatchOption(
        key = "targetFilePath",
        default = null,
        title = "Target file path",
        description = "The path to the file to make the changes in relative to the APK root.",
        required = true,
    )

    // TODO: Instead of stringArrayOption, use a custom option type to work around
    //  https://github.com/ReVanced/revanced-library/issues/48.
    //  Replace the custom option type with a stringArrayOption once the issue is resolved.
    private val patterns by registerNewPatchOption<app.revanced.patcher.patch.Patch<*>, List<String>>(
        key = "patterns",
        default = null,
        title = "patterns",
        description = """
            Hexadecimal patterns to search for and replace in the target file.
            
            A pattern is a sequence of case insensitive strings representing hexadecimal bytes separated by spaces.
            An example pattern is 'aa 01 02 FF'.

            Every pattern must be followed by a pipe ('|') and the replacement pattern.
            The replacement pattern must have the same length as the original pattern.

            Full example of a valid input:
            'aa 01 02 FF|00 00 00 00'
        """.trimIndentMultiline(),
        required = true,
        valueType = "StringArray",
    )

    override fun execute(context: ResourceContext) {
        val targetFile = context[targetFilePath!!, true]
        // TODO: Use a file channel to read and write the file instead of reading the whole file into memory,
        //  in order to reduce memory usage.
        val targetFileBytes = targetFile.readBytes()

        patterns!!.map(::replacementOf).forEach { replacement ->
            replacement.replacePattern(targetFileBytes)
        }

        targetFile.writeBytes(targetFileBytes)
    }

    private class Replacement private constructor(pattern: String, replacementPattern: String) {
        val pattern = pattern.toByteArrayPattern()
        val replacementPattern = replacementPattern.toByteArrayPattern()

        init {
            if (this.pattern.size != this.replacementPattern.size) {
                throw PatchException("Pattern and replacement pattern must have the same length: $pattern")
            }
        }

        fun replacePattern(haystack: ByteArray) {
            val startIndex = indexOfPatternIn(haystack)

            if (startIndex == -1) {
                throw PatchException("Pattern not found in target file: $pattern")
            }

            replacementPattern.copyInto(haystack, startIndex, 0, replacementPattern.size)
        }

        // TODO: Allow searching in a file channel instead of a byte array to reduce memory usage.
        /**
         * Returns the index of the first occurrence of [pattern] in the haystack
         * using the Boyer-Moore algorithm.
         *
         * @param haystack The array to search in.
         *
         * @return The index of the first occurrence of the [pattern] in the haystack or -1
         * if the [pattern] is not found.
         */
        private fun indexOfPatternIn(haystack: ByteArray): Int {
            val needle = pattern

            val haystackLength = haystack.size - 1
            val needleLength = needle.size - 1
            val right = IntArray(256) { -1 }

            for (i in 0 until needleLength) right[needle[i].toInt()] = i

            var skip: Int
            for (i in 0..haystackLength - needleLength) {
                skip = 0

                for (j in needleLength - 1 downTo 0)
                    if (needle[j] != haystack[i + j]) {
                        skip = max(1, j - right[haystack[i + j].toInt()])

                        break
                    }

                if (skip == 0) return i
            }
            return -1
        }

        companion object {
            private fun String.toByteArrayPattern() = try {
                split(" ").map { it.toByte(16) }.toByteArray()
            } catch (e: NumberFormatException) {
                throw PatchException(
                    "Could not parse pattern: $this.  A pattern is a sequence of case insensitive strings " +
                        "representing hexadecimal bytes separated by spaces",
                )
            }

            fun replacementOf(patternAndReplacement: String): Replacement {
                val (pattern, replacementPattern) = try {
                    patternAndReplacement.split("|")
                } catch (e: Exception) {
                    throw PatchException(
                        "Invalid pattern: $patternAndReplacement. " +
                            "Every pattern must be followed by a pipe ('|') and the replacement pattern",
                    )
                }

                return Replacement(pattern, replacementPattern)
            }
        }
    }
}
