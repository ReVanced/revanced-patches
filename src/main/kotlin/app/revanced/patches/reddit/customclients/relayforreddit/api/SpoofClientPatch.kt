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

    val loginActivityClientIdResult by loginActivityClientIdFingerprint
    val getLoggedInBearerTokenResult by getLoggedInBearerTokenFingerprint
    val getLoggedOutBearerTokenResult by getLoggedOutBearerTokenFingerprint
    val getRefreshTokenResult by getRefreshTokenFingerprint
    val setRemoteConfigResult by setRemoteConfigFingerprint
    val redditCheckDisableAPIResult by redditCheckDisableAPIFingerprint

    val clientId by it

    execute {
        // region Patch client id.

        setOf(
            loginActivityClientIdResult,
            getLoggedInBearerTokenResult,
            getLoggedOutBearerTokenResult,
            getRefreshTokenResult,
        ).forEach { result ->
            val clientIdIndex = result.scanResult.stringsScanResult!!.matches.first().index
            result.mutableMethod.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                result.mutableMethod.replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$clientId\"",
                )
            }
        }

        // endregion

        // region Patch miscellaneous.

        // Do not load remote config which disables OAuth login remotely.
        setRemoteConfigResult.mutableMethod.addInstructions(0, "return-void")

        // Prevent OAuth login being disabled remotely.
        val checkIsOAuthRequestIndex = redditCheckDisableAPIResult.scanResult.patternScanResult!!.startIndex

        redditCheckDisableAPIResult.mutableMethod.apply {
            val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(checkIsOAuthRequestIndex, BuilderInstruction10t(Opcode.GOTO, returnNextChain))
        }

        // endregion
    }
}
