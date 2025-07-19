package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

val spoofClientPatch = spoofClientPatch(redirectUri = "dbrady://relay") {
    compatibleWith(
        "free.reddit.news",
        "reddit.news",
    )

    val clientId by it

    execute {
        // region Patch client id.

        setOf(
            loginActivityClientIdFingerprint,
            getLoggedInBearerTokenFingerprint,
            getLoggedOutBearerTokenFingerprint,
            getRefreshTokenFingerprint,
        ).forEach { fingerprint ->
            val clientIdIndex = fingerprint.stringMatches!!.first().index
            fingerprint.method.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                fingerprint.method.replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$clientId\"",
                )
            }
        }
        // Redirects from oauth to WWW domain are bugged causing auth problems on login.
        // Manually rewrite the URLs to fix this.
        // Only patch specific login-related methods to fix auth redirects
        // while preserving logged-out method for offline browsing
        listOf(
            loginActivityClientIdFingerprint,
            getLoggedInBearerTokenFingerprint
        ).forEach { fingerprint ->
            fingerprint.method.implementation!!.instructions.forEachIndexed { index, instruction ->
                if (instruction.opcode.name == "CONST_STRING") {
                    val reference = instruction.getReference<StringReference>()
                    reference?.string?.let { stringValue ->
                        if (stringValue.contains("oauth.reddit.com")) {
                            val register = (instruction as OneRegisterInstruction).registerA
                            val newUrl = stringValue.replace("oauth.reddit.com", "www.reddit.com")
                            fingerprint.method.replaceInstruction(index, "const-string v$register, \"$newUrl\"")
                        }
                    }
                }
            }
        }

        // endregion

        // region Patch miscellaneous.

        // Do not load remote config which disables OAuth login remotely.
        setRemoteConfigFingerprint.method.addInstructions(0, "return-void")

        // Prevent OAuth login being disabled remotely.
        val checkIsOAuthRequestIndex = redditCheckDisableAPIFingerprint.patternMatch!!.startIndex

        redditCheckDisableAPIFingerprint.method.apply {
            val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(checkIsOAuthRequestIndex, BuilderInstruction10t(Opcode.GOTO, returnNextChain))
        }

        // endregion
    }
}
