package app.revanced.patches.samsung.radio.restrictions.device

import app.revanced.patcher.fingerprint
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.fromMethodReference
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val checkDeviceFingerprint = fingerprint {
    returns("Z")
    custom { method, _ ->
        /* Check for methods call to:
            - Landroid/os/SemSystemProperties;->getSalesCode()Ljava/lang/String;
            - Landroid/os/SemSystemProperties;->getCountryIso()Ljava/lang/String;
        */

        val impl = method.implementation ?: return@custom false

        // Track which target methods we've found
        val foundMethods = mutableSetOf<MethodCall>()

        // Scan method instructions for calls to our target methods
        for (instr in impl.instructions) {
            val ref = instr.getReference<MethodReference>() ?: continue
            val mc = fromMethodReference<MethodCall>(ref) ?: continue

            if (mc == MethodCall.GetSalesCode || mc == MethodCall.GetCountryIso) {
                foundMethods.add(mc)

                // If we found both methods, return success
                if (foundMethods.size == 2) {
                    return@custom true
                }
            }
        }

        // Only match if both methods are present
        return@custom false
    }
}

// Information about method calls we want to replace
private enum class MethodCall(
    override val definedClassName: String,
    override val methodName: String,
    override val methodParams: Array<String>,
    override val returnType: String,
) : IMethodCall {
    GetSalesCode(
        "Landroid/os/SemSystemProperties;",
        "getSalesCode",
        arrayOf(),
        "Ljava/lang/String;",
    ),
    GetCountryIso(
        "Landroid/os/SemSystemProperties;",
        "getCountryIso",
        arrayOf(),
        "Ljava/lang/String;",
    ),
}