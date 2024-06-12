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

    val loginActivityClientIdFingerprintResult by loginActivityClientIdFingerprint
    val getLoggedInBearerTokenFingerprintResult by getLoggedInBearerTokenFingerprint
    val getLoggedOutBearerTokenFingerprintResult by getLoggedOutBearerTokenFingerprint
    val getRefreshTokenFingerprintResult by getRefreshTokenFingerprint
    val setRemoteConfigFingerprintResult by setRemoteConfigFingerprint
    val redditCheckDisableAPIFingerprintResult by redditCheckDisableAPIFingerprint

    val clientId by it

    execute {
        // region Patch client id.

        setOf(
            loginActivityClientIdFingerprintResult,
            getLoggedInBearerTokenFingerprintResult,
            getLoggedOutBearerTokenFingerprintResult,
            getRefreshTokenFingerprintResult,
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
        setRemoteConfigFingerprintResult.mutableMethod.addInstructions(0, "return-void")

        // Prevent OAuth login being disabled remotely.
        val checkIsOAuthRequestIndex = redditCheckDisableAPIFingerprintResult.scanResult.patternScanResult!!.startIndex

        redditCheckDisableAPIFingerprintResult.mutableMethod.apply {
            val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(checkIsOAuthRequestIndex, BuilderInstruction10t(Opcode.GOTO, returnNextChain))
        }

        // endregion
    }
}
