package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofClientPatch = spoofClientPatch(redirectUri = "dbrady://relay") { clientIdOption ->
    compatibleWith(
        "free.reddit.news",
        "reddit.news",
    )

    val clientId by clientIdOption

    apply {
        // region Patch client id.

        listOf(
            loginActivityClientIdMethodMatch,
            getLoggedInBearerTokenMethodMatch,
            getLoggedOutBearerTokenMethodMatch,
            getRefreshTokenMethodMatch,
        ).forEach { match ->
            val clientIdIndex = match.indices.first()
            val clientIdRegister = match.method.getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

            match.method.replaceInstruction(clientIdIndex, "const-string v$clientIdRegister, \"$clientId\"")
        }

        // endregion

        // region Patch miscellaneous.

        // Do not load remote config which disables OAuth login remotely.
        setRemoteConfigMethod.returnEarly()

        // Prevent OAuth login being disabled remotely.
        redditCheckDisableAPIMethod.apply {
            val checkIsOAuthRequestIndex = indexOfFirstInstructionOrThrow(Opcode.IF_EQZ)
            val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(checkIsOAuthRequestIndex, BuilderInstruction10t(Opcode.GOTO, returnNextChain))
        }

        // endregion
    }
}
