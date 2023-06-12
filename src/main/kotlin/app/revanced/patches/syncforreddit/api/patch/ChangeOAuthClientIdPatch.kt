package app.revanced.patches.syncforreddit.api.patch

import android.os.Environment
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.syncforreddit.api.fingerprints.GetAuthorizationStringFingerprint
import app.revanced.patches.syncforreddit.api.fingerprints.GetBearerTokenFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.StringReference
import java.io.File
import java.util.*

@Patch
@Name("change-oauth-client-id")
@Description("Changes the OAuth client ID.")
@Compatibility(
    [
        Package("com.laurencedawson.reddit_sync"),
        Package("com.laurencedawson.reddit_sync.pro")
    ]
)
@Version("0.0.1")
class ChangeOAuthClientIdPatch : BytecodePatch(
    listOf(GetAuthorizationStringFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        if (clientId == null) {
            // Test if on Android
            try {
                Class.forName("android.os.Environment")
            } catch (e: ClassNotFoundException) {
                return PatchResultError("No client ID provided")
            }

            File(Environment.getExternalStorageDirectory(), "reddit_client_id_revanced.txt").also {
                if (it.exists()) return@also

                val error = """
                    In order to use this patch, you need to provide a client ID.
                    You can do this by creating a file at ${it.absolutePath} with the client ID as its content.
                    Alternatively, you can provide the client ID using patch options.
                    
                    You can get your client ID from https://www.reddit.com/prefs/apps.
                    The application type has to be "installed app" and the redirect URI has to be set to "http://redditsync/auth"
                """.trimIndent()

                return PatchResultError(error)
            }.let { clientId = it.readText().trim() }
        }

        GetAuthorizationStringFingerprint.result?.also { result ->
            GetBearerTokenFingerprint.also { it.resolve(context, result.classDef) }.result?.mutableMethod?.apply {
                val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))
                addInstructions(
                    0,
                    """
                         const-string v0, "Basic $auth"
                         return-object v0
                    """
                )
            } ?: return PatchResultError("Could not find required method to patch.")
        }?.let {
            val occurrenceIndex = it.scanResult.stringsScanResult!!.matches.first().index

            it.mutableMethod.apply {
                val authorizationStringInstruction = getInstruction<ReferenceInstruction>(occurrenceIndex)
                val targetRegister = (authorizationStringInstruction as OneRegisterInstruction).registerA
                val reference = authorizationStringInstruction.reference as StringReference

                val newAuthorizationUrl = reference.string.replace(
                    "client_id=.*?&".toRegex(),
                    "client_id=${clientId!!}&"
                )

                replaceInstruction(
                    occurrenceIndex,
                    "const-string v$targetRegister, \"$newAuthorizationUrl\""
                )
            }
        } ?: return PatchResultError("Could not find required method to patch.")
        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        var clientId by option(
            PatchOption.StringOption(
                "client-id",
                null,
                "OAuth client ID",
                "The client ID to use for OAuth."
            )
        )
    }
}
