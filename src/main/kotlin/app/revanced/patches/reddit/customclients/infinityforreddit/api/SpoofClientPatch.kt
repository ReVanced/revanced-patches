package app.revanced.patches.reddit.customclients.infinityforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints.APIUtilsFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation

@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "infinity://localhost",
    clientIdFingerprints = setOf(APIUtilsFingerprint),
    compatiblePackages = setOf(CompatiblePackage("ml.docilealligator.infinityforreddit"))
) {
    override fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
        first().mutableClass.methods.apply {
            val getClientIdMethod = single { it.name == "getId" }.also(::remove)

            val newGetClientIdMethod = ImmutableMethod(
                getClientIdMethod.definingClass,
                getClientIdMethod.name,
                null,
                getClientIdMethod.returnType,
                AccessFlags.PUBLIC or AccessFlags.STATIC,
                null,
                null,
                ImmutableMethodImplementation(
                    1,
                    """
                        const-string v0, "$clientId"
                        return-object v0
                    """.toInstructions(getClientIdMethod),
                    null,
                    null,
                ),
            ).toMutable()

            add(newGetClientIdMethod)
        }
    }
}
