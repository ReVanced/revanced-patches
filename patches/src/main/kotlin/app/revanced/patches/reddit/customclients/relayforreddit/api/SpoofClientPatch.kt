package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.matchOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.sun.org.apache.bcel.internal.generic.InstructionConst.getInstruction

val spoofClientPatch = spoofClientPatch(redirectUri = "dbrady://relay") {
    compatibleWith(
        "free.reddit.news",
        "reddit.news",
    )

    val clientId by it

    execute {
        // region Patch client id.

        setOf(
            loginActivityClientIdFingerprint.matchOrThrow,
            getLoggedInBearerTokenFingerprint.matchOrThrow,
            getLoggedOutBearerTokenFingerprint.matchOrThrow,
            getRefreshTokenFingerprint.matchOrThrow,
        ).forEach { match ->
            val clientIdIndex = match.stringMatches!!.first().index
            match.method.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                match.method.replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$clientId\"",
                )
            }
        }

        // endregion

        // region Patch miscellaneous.

        // Do not load remote config which disables OAuth login remotely.
        setRemoteConfigFingerprint.matchOrThrow.method.addInstructions(0, "return-void")

        // Prevent OAuth login being disabled remotely.
        val checkIsOAuthRequestIndex = redditCheckDisableAPIFingerprint.matchOrThrow.patternMatch!!.startIndex

        redditCheckDisableAPIFingerprint.matchOrThrow.method.apply {
            val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(checkIsOAuthRequestIndex, BuilderInstruction10t(Opcode.GOTO, returnNextChain))
        }

        // endregion
    }
}
