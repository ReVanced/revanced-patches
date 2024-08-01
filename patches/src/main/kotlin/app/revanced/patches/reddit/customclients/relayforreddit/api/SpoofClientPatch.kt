package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "dbrady://relay") {
    compatibleWith(
        "free.reddit.news",
        "reddit.news",
    )

    val loginActivityClientIdMatch by loginActivityClientIdFingerprint()
    val getLoggedInBearerTokenMatch by getLoggedInBearerTokenFingerprint()
    val getLoggedOutBearerTokenMatch by getLoggedOutBearerTokenFingerprint()
    val getRefreshTokenMatch by getRefreshTokenFingerprint()
    val setRemoteConfigMatch by setRemoteConfigFingerprint()
    val redditCheckDisableAPIMatch by redditCheckDisableAPIFingerprint()

    val clientId by it

    execute {
        // region Patch client id.

        setOf(
            loginActivityClientIdMatch,
            getLoggedInBearerTokenMatch,
            getLoggedOutBearerTokenMatch,
            getRefreshTokenMatch,
        ).forEach { match ->
            val clientIdIndex = match.stringMatches!!.first().index
            match.mutableMethod.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                match.mutableMethod.replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$clientId\"",
                )
            }
        }

        // endregion

        // region Patch miscellaneous.

        // Do not load remote config which disables OAuth login remotely.
        setRemoteConfigMatch.mutableMethod.addInstructions(0, "return-void")

        // Prevent OAuth login being disabled remotely.
        val checkIsOAuthRequestIndex = redditCheckDisableAPIMatch.patternMatch!!.startIndex

        redditCheckDisableAPIMatch.mutableMethod.apply {
            val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(checkIsOAuthRequestIndex, BuilderInstruction10t(Opcode.GOTO, returnNextChain))
        }

        // endregion
    }
}
