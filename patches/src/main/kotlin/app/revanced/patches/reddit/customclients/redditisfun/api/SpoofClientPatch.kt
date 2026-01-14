package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.Match
import app.revanced.patcher.MatchBuilder
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "redditisfun://auth") { clientIdOption ->
    compatibleWith(
        "com.andrewshu.android.reddit",
        "com.andrewshu.android.redditdonation",
    )

    val clientId by clientIdOption

    apply {
        // region Patch client id.

        /**
         * Replaces a one register instruction with a const-string instruction
         * at the index returned by [getReplacementIndex].
         *
         * @param string The string to replace the instruction with.
         * @param getReplacementIndex A function that returns the index of the instruction to replace
         * using the [Match.indices] list from the [Match].
         */
        fun MatchBuilder.replaceWith(
            string: String,
            getReplacementIndex: List<Int>.() -> Int,
        ) = method.apply {
            val replacementIndex = indices.getReplacementIndex()
            val clientIdRegister = getInstruction<OneRegisterInstruction>(replacementIndex).registerA

            replaceInstruction(replacementIndex, "const-string v$clientIdRegister, \"$string\"")
        }

        // Patch OAuth authorization.
        buildAuthorizationStringMethodMatch.replaceWith(clientId!!) { first() + 4 }

        // Path basic authorization.
        basicAuthorizationMethodMatch.replaceWith("$clientId:") { last() + 7 }

        // endregion

        // region Patch user agent.

        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        getUserAgentMethod.returnEarly(userAgent)

        // endregion

        // region Patch miscellaneous.

        // Reddit messed up and does not append a redirect uri to the authorization url to old.reddit.com/login.
        // Replace old.reddit.com with www.reddit.com to fix this.
        buildAuthorizationStringMethodMatch.method.apply {
            val index = indexOfFirstInstructionOrThrow {
                getReference<StringReference>()?.contains("old.reddit.com") == true
            }

            val targetRegister = getInstruction<OneRegisterInstruction>(index).registerA
            replaceInstruction(
                index,
                "const-string v$targetRegister, \"https://www.reddit.com/api/v1/authorize.compact\"",
            )
        }

        // endregion
    }
}
