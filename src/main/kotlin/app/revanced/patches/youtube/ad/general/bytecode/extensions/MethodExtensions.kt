package app.revanced.patches.youtube.ad.general.bytecode.extensions

import app.revanced.patcher.extensions.softCompareTo
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.Reference
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference

internal object MethodExtensions {
    internal fun MutableClass.findMutableMethodOf(
        method: Method
    ) = this.methods.first {
        it.softCompareTo(
            ImmutableMethodReference(
                method.definingClass, method.name, method.parameters, method.returnType
            )
        )
    }

    internal inline fun <reified T : Reference> Instruction.toDescriptor(): String {
        val reference = (this as ReferenceInstruction).reference
        return when (T::class) {
            MethodReference::class -> {
                val methodReference = reference as MethodReference
                "${methodReference.definingClass}->${methodReference.name}(${
                    methodReference.parameterTypes.joinToString(
                        ""
                    ) { it }
                })${methodReference.returnType}"
            }

            FieldReference::class -> {
                val fieldReference = reference as FieldReference
                "${fieldReference.definingClass}->${fieldReference.name}:${fieldReference.type}"
            }

            else -> throw PatchResultError("Unsupported reference type")
        }
    }
}