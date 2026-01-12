package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.`Spoof client`
import app.revanced.patches.shared.misc.string.replaceStringPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.mutable.MutableMethod

val spoofClientPatch = `Spoof client`(redirectUri = "http://baconreader.com/auth") { clientIdOption ->
    dependsOn(
        // Redirects from SSL to WWW domain are bugged causing auth problems.
        // Manually rewrite the URLs to fix this.
        replaceStringPatch("ssl.reddit.com", "www.reddit.com")
    )

    compatibleWith(
        "com.onelouder.baconreader",
        "com.onelouder.baconreader.premium",
    )

    val clientId by clientIdOption

    apply {
        fun MutableMethod.patch(targetString: String, replacementString: String) {
            val clientIdIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING && getReference<StringReference>()?.string == targetString
            }

            val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA
            replaceInstruction(
                clientIdIndex,
                "const-string v$clientIdRegister, \"$replacementString\"",
            )
        }

        // Patch client id in authorization url.
        getAuthorizationUrlMethod.patch("client_id=zACVn0dSFGdWqQ", "client_id=$clientId")

        // Patch client id for access token request.
        requestTokenMethod.patch("zACVn0dSFGdWqQ", clientId!!)
    }
}
