package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.`Spoof client`
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.mutable.MutableMethod

@Suppress("unused")
val spoofClientPatch = `Spoof client`(redirectUri = "redditisfun://auth") { clientIdOption ->
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
         * using the [Match.StringMatch] list from the [Match].
         */
        fun MutableMethod.replaceWith(
            string: String,
            offset: Int,
            getReplacementIndex: String
        ) {
            val anchorIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING && getReference<StringReference>()?.string == string
            }

            val targetIndex = anchorIndex + offset
            val clientIdRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            replaceInstruction(targetIndex, "const-string v$clientIdRegister, \"$getReplacementIndex\"")
        }

        // Patch OAuth authorization.
        buildAuthorizationStringMethod.replaceWith(
            string = "yyOCBp.RHJhDKd",
            offset = 4,
            getReplacementIndex = clientId!!
        )

        // Path basic authorization.
        basicAuthorizationMethod.replaceWith(
            string = "fJOxVwBUyo*=f:<OoejWs:AqmIJ",
            offset = 7,
            getReplacementIndex = "$clientId:"
        )

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
        buildAuthorizationStringMethod.apply {
            val index = indexOfFirstInstructionOrThrow {
                getReference<StringReference>()?.string?.contains("old.reddit.com") == true
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
