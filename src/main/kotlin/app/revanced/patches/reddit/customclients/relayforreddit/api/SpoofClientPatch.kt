package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction


@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "dbrady://relay",
    miscellaneousFingerprints = setOf(
        SetRemoteConfigFingerprint,
        RedditCheckDisableAPIFingerprint
    ),
    clientIdFingerprints = setOf(
        LoginActivityClientIdFingerprint,
        GetLoggedInBearerTokenFingerprint,
        GetLoggedOutBearerTokenFingerprint,
        GetRefreshTokenFingerprint
    ),
    compatiblePackages = setOf(
        CompatiblePackage("free.reddit.news"),
        CompatiblePackage("reddit.news")
    )
) {
    override fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
        forEach {
            val clientIdIndex = it.scanResult.stringsScanResult!!.matches.first().index
            it.mutableMethod.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                it.mutableMethod.replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$clientId\""
                )
            }
        }
    }

    override fun Set<MethodFingerprintResult>.patchMiscellaneous(context: BytecodeContext) {
        // Do not load remote config which disables OAuth login remotely.
        first().mutableMethod.addInstructions(0, "return-void")

        // Prevent OAuth login being disabled remotely.
        last().let {
            val checkIsOAuthRequestIndex = it.scanResult.patternScanResult!!.startIndex

            it.mutableMethod.apply {
                val returnNextChain = getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
                replaceInstruction(
                    checkIsOAuthRequestIndex,
                    BuilderInstruction10t(
                        Opcode.GOTO,
                        returnNextChain
                    )
                )
            }
        }
    }
}