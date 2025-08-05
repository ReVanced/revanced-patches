package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption

val imgurAlbumsPatch = bytecodePatch(
    name = "Use public imgur API",
    description = "Fix imgur albums not loading."
) {
    compatibleWith(
        "com.andrewshu.android.reddit",
        "com.andrewshu.android.redditdonation",
    )

    val clientId by stringOption(
        key = "imgur-client-id",
        // Obtained from: https://s.imgur.com/desktop-assets/js/main.[snip].js | grep apiClientId
        default = "546c25a59c58ad7",
        title = "Imgur client ID",
        description = "The default value should work for most users",
        required = true,
    )

    execute {
        val m = imgurApiFingerprint.method
        m.removeInstructions(m.instructions.size)
        val androidNetUriBuilder = "android/net/Uri\$Builder"
        m.addInstructions(0, """
    new-instance v0, L$androidNetUriBuilder;
    invoke-direct {v0}, L$androidNetUriBuilder;-><init>()V
    const-string v1, "https"
    invoke-virtual {v0, v1}, L$androidNetUriBuilder;->scheme(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object v0
    const-string v1, "api.imgur.com"
    invoke-virtual {v0, v1}, L$androidNetUriBuilder;->authority(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object v0
    const-string v1, "3"
    invoke-virtual {v0, v1}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object v0
    if-eqz p1, :cond_0
    const-string p1, "gallery"
    invoke-virtual {v0, p1}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    :cond_0
    const-string p1, "album"
    invoke-virtual {v0, p1}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object p1
    invoke-virtual {p1, p0}, L$androidNetUriBuilder;->appendPath(Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object p0
    const-string v0, "client_id"
    const-string v1, "$clientId"
    invoke-virtual {p0, v0, v1}, L$androidNetUriBuilder;->appendQueryParameter(Ljava/lang/String;Ljava/lang/String;)L$androidNetUriBuilder;
    move-result-object p0
    invoke-virtual {p0}, L$androidNetUriBuilder;->build()Landroid/net/Uri;
    move-result-object p0
    return-object p0
        """.trimIndent())
    }
}

/*
The original contents of the method decodes an obfuscated string, you can use the following script
to decode the value:

import kotlin.math.pow
object StringDecryptor {
    /**
     * Decrypts a string using a position-dependent Caesar-like cipher.
     * Handles lowercase, uppercase, and other characters differently.
     * Replaces '>' with '_' at the end.
     */
    fun w(input: String): String {
        val builder = StringBuilder()
        val length = input.length

        // Note: Smali loop starts from 1, accesses charAt(i-1), uses 'i' for shift
        for (i in 1..length) {
            val charIndex = i - 1 // Index of the character to process
            var charCode = input[charIndex].code // Get character's Unicode code point
            var base: Int
            val mod: Int

            when (input[charIndex]) {
                in 'a'..'z' -> {
                    base = 'a'.code
                    mod = 26
                    charCode -= base // Normalize to 0-25
                }
                in 'A'..'Z' -> {
                    base = 'A'.code
                    mod = 26
                    charCode -= base // Normalize to 0-25
                }
                else -> {
                    // Handles other characters based on space ' '
                    // Corresponds to the Smali block L3
                    base = ' '.code
                    mod = 33
                    charCode -= base // Normalize relative to space
                }
            }

            // Apply the position-dependent shift (subtracting loop counter 'i')
            // The formula (val - shift + mod) % mod handles potential negative results correctly
            val shiftedCode = (charCode - i + mod) % mod

            // Add the base back to get the final character code
            val finalCode = shiftedCode + base

            builder.append(finalCode.toChar())
        }

        // Final replacement as seen in the Smali code
        return builder.toString().replace('>', '_')
    }

    /**
     * Unscrambles a string based on an index mapping array.
     * If input is "cba" and map is [2, 0, 1], output is "abc".
     */
    fun z(scrambledString: String, indexMap: IntArray): String {
        val length = indexMap.size
        // Create an inverse map: targetIndices[original_index] = current_index
        val targetIndices = IntArray(length)
        for (i in 0 until length) {
            targetIndices[indexMap[i]] = i
        }

        val builder = StringBuilder(length)
        // Build the unscrambled string using the inverse map
        for (i in 0 until length) {
            builder.append(scrambledString[targetIndices[i]])
        }
        return builder.toString()
    }

    fun decode(s: String, offsets: IntArray): String {
        // Values produced by decompiler are offset
        val decompilerMult = 2.0.pow(24).toInt()
        // If you extract the values from the smali directly no offset is needed:
        //val decompilerMult = 1

        // 1. Unscramble the string using z()
        val unscrambledString = z(s, offsets.map { it / decompilerMult }.toIntArray())
        // 2. Decrypt the unscrambled string using w()
        return w(unscrambledString)
    }
}

StringDecryptor.decode("krltdwtl2eikrcbfsg>", intArrayOf(
    100663296,
    16777216,
    117440512,
    167772160,
    150994944,
    67108864,
    268435456,
    33554432,
    50331648,
    184549376,
    218103808,
    83886080,
    134217728,
    234881024,
    0,
    301989888,
    201326592,
    285212672,
    251658240,
)) // Output: api.redditisfun.com
 */