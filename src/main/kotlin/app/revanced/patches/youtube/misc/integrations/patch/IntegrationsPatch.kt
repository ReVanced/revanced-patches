package app.revanced.patches.youtube.misc.integrations.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.patches.youtube.misc.integrations.fingerprints.InitFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.ServiceFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.StandalonePlayerFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodImplementation

@Name("integrations")
@Description("Applies mandatory patches to implement the ReVanced integrations into the application.")
@IntegrationsCompatibility
@Version("0.0.1")
class IntegrationsPatch : BytecodePatch(
    listOf(
        InitFingerprint, StandalonePlayerFingerprint, ServiceFingerprint
    )
) {
    companion object {
        private const val INTEGRATIONS_DESCRIPTOR = "Lapp/revanced/integrations/utils/ReVancedUtils;"
    }

    override fun execute(context: BytecodeContext): PatchResult {
        if (context.findClass(INTEGRATIONS_DESCRIPTOR) == null)
            return PatchResultError("Integrations have not been merged yet. This patch can not succeed without merging the integrations.")

        arrayOf(InitFingerprint, StandalonePlayerFingerprint, ServiceFingerprint).map {
            it to (it.result ?: return PatchResultError("${it.name} failed to resolve"))
        }.forEach { (fingerprint, result) ->
            with(result.mutableMethod) {
                // parameter which holds the context
                val contextParameter = if (fingerprint == ServiceFingerprint) parameters.size else 1
                // register which holds the context
                val contextRegister = implementation!!.registerCount - contextParameter

                addInstruction(
                    0,
                    "sput-object v$contextRegister, $INTEGRATIONS_DESCRIPTOR->context:Landroid/content/Context;"
                )
            }
        }

        return PatchResultSuccess()
    }
}