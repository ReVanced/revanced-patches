package app.revanced.patches.all.misc.hex

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.registerNewPatchOption
import app.revanced.patches.shared.misc.hex.BaseHexPatch
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.patcher.patch.Patch as PatchClass

@Patch(
    name = "Hex",
    description = "Replaces a hexadecimal patterns of bytes of files in an APK.",
    use = false,
)
@Suppress("unused")
class HexPatch : BaseHexPatch() {
    // TODO: Instead of stringArrayOption, use a custom option type to work around
    //  https://github.com/ReVanced/revanced-library/issues/48.
    //  Replace the custom option type with a stringArrayOption once the issue is resolved.
    private val replacementsOption by registerNewPatchOption<PatchClass<*>, List<String>>(
        key = "replacements",
        title = "replacements",
        description = """
            Hexadecimal patterns to search for and replace with another in a target file.
            
            A pattern is a sequence of case insensitive strings, each representing hexadecimal bytes, separated by spaces.
            An example pattern is 'aa 01 02 FF'.

            Every pattern must be followed by a pipe ('|'), the replacement pattern,
            another pipe ('|'), and the path to the file to make the changes in relative to the APK root. 
            The replacement pattern must have the same length as the original pattern.

            Full example of a valid input:
            'aa 01 02 FF|00 00 00 00|path/to/file'
        """.trimIndentMultiline(),
        required = true,
        valueType = "StringArray",
    )

    override val replacements
        get() = replacementsOption!!.map { from ->
            val (pattern, replacementPattern, targetFilePath) = try {
                from.split("|", limit = 3)
            } catch (e: Exception) {
                throw PatchException(
                    "Invalid input: $from.\n" +
                        "Every pattern must be followed by a pipe ('|'), " +
                        "the replacement pattern, another pipe ('|'), " +
                        "and the path to the file to make the changes in relative to the APK root. ",
                )
            }

            Replacement(pattern, replacementPattern, targetFilePath)
        }
}
