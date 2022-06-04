package app.revanced.patches.youtube.ad.general.bytecode.utils

import app.revanced.patcher.extensions.or
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.MethodImplementation
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodParameter

internal object MethodUtils {
    internal fun createMutableMethod(
        definingClass: String, name: String, returnType: String, parameter: String, implementation: MethodImplementation
    ) = ImmutableMethod(
        definingClass, name, listOf(
            ImmutableMethodParameter(
                parameter, null, null
            )
        ), returnType, AccessFlags.PRIVATE or AccessFlags.STATIC, null, null, implementation
    ).toMutable()
}