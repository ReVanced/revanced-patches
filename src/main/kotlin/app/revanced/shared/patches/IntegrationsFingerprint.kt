package app.revanced.shared.patches

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.shared.patches.AbstractIntegrationsPatch.IntegrationsFingerprint.RegisterResolver
import org.jf.dexlib2.iface.Method

@Description("Applies mandatory patches to implement the ReVanced integrations into the application.")
@Version("0.0.1")
abstract class AbstractIntegrationsPatch(
    private val integrationsDescriptor: String,
    private val hooks: Iterable<IntegrationsFingerprint>
) : BytecodePatch(hooks) {
    /**
     * [MethodFingerprint] for integrations.
     *
     * @param contextRegisterResolver A [RegisterResolver] to get the register.
     * @see MethodFingerprint
     */
    abstract class IntegrationsFingerprint(
        strings: Iterable<String>? = null,
        customFingerprint: ((methodDef: Method) -> Boolean)? = null,
        private val contextRegisterResolver: (Method) -> Int = object : RegisterResolver {}
    ) : MethodFingerprint(strings = strings, customFingerprint = customFingerprint) {
        fun invoke(integrationsDescriptor: String): PatchResult {
            result?.mutableMethod?.let { method ->
                val contextRegister = contextRegisterResolver(method)

                method.addInstruction(
                    0,
                    "sput-object v$contextRegister, " +
                            "$integrationsDescriptor->context:Landroid/content/Context;"
                )
            } ?: return PatchResultError("Could not find hook target fingerprint.")

            return PatchResultSuccess()
        }

        interface RegisterResolver : (Method) -> Int {
            override operator fun invoke(method: Method) = method.implementation!!.registerCount - 1
        }
    }

    override fun execute(context: BytecodeContext): PatchResult {
        if (context.findClass(integrationsDescriptor) == null) return MISSING_INTEGRATIONS

        for (hook in hooks) hook.invoke(integrationsDescriptor).let {
            if (it is PatchResultError) return it
        }

        return PatchResultSuccess()
    }

    private companion object {
        val MISSING_INTEGRATIONS = PatchResultError(
            "Integrations have not been merged yet. " +
                    "This patch can not succeed without merging the integrations."
        )
    }
}