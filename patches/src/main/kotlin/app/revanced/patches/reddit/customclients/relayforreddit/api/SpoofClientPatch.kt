package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

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
            loginActivityClientIdMethod,
            getLoggedInBearerTokenMethod,
            getLoggedOutBearerTokenMethod,
            getRefreshTokenMethod,
        ).forEach { method ->
            method.apply {
                val clientIdIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.CONST_STRING && getReference<StringReference>()?.string == "dj-xCIZQYiLbEg"
                }
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$clientId\"",
                )
            }
        }

        // endregion

        // region Patch miscellaneous.

        // Do not load remote config which disables OAuth login remotely.
        setRemoteConfigMethod.addInstructions(0, "return-void")

        // Prevent OAuth login being disabled remotely.
        redditCheckDisableAPIMethod.apply {
            val checkIsOAuthRequestIndex = indexOfFirstInstructionOrThrow(Opcode.IF_EQZ)
            val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(checkIsOAuthRequestIndex, BuilderInstruction10t(Opcode.GOTO, returnNextChain))
        }

        // endregion
    }
}
