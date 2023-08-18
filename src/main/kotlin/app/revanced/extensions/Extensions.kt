package app.revanced.extensions

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import com.android.tools.smali.dexlib2.util.MethodUtil
import org.w3c.dom.Node

/**
 * Return [PatchException] from a [MethodFingerprint].
 *
 * @return The [PatchException] for the [MethodFingerprint].
 */
internal fun MethodFingerprint.toErrorResult() = PatchException("Failed to resolve $name")

/**
 * Find the [MutableMethod] from a given [Method] in a [MutableClass].
 *
 * @param method The [Method] to find.
 * @return The [MutableMethod].
 */
internal fun MutableClass.findMutableMethodOf(method: Method) = this.methods.first {
    MethodUtil.methodSignaturesMatch(it, method)
}

/**
 * apply a transform to all methods of the class
 *
 * @param transform the transformation function. original method goes in, transformed method goes out
 */
internal fun MutableClass.transformMethods(transform: MutableMethod.() -> MutableMethod) {
    val transformedMethods = methods.map { it.transform() }
    methods.clear()
    methods.addAll(transformedMethods)
}

internal fun Node.doRecursively(action: (Node) -> Unit) {
    action(this)
    for (i in 0 until this.childNodes.length) this.childNodes.item(i).doRecursively(action)
}

internal fun MutableMethod.injectHideViewCall(
    insertIndex: Int,
    viewRegister: Int,
    classDescriptor: String,
    targetMethod: String
) = addInstruction(
    insertIndex,
    "invoke-static { v$viewRegister }, $classDescriptor->$targetMethod(Landroid/view/View;)V"
)

internal fun Method.findIndexForIdResource(resourceName: String): Int {
    fun getIdResourceId(resourceName: String) = ResourceMappingPatch.resourceMappings.single {
        it.type == "id" && it.name == resourceName
    }.id

    return indexOfFirstConstantInstructionValue(getIdResourceId(resourceName))
}

/**
 * @return the first constant instruction with the value, or -1 if not found.
 */
fun Method.indexOfFirstConstantInstructionValue(constantValue: Long): Int {
    return implementation?.let {
        it.instructions.indexOfFirst { instruction ->
            instruction.opcode == Opcode.CONST && (instruction as WideLiteralInstruction).wideLiteral == constantValue
        }
    } ?: -1
}

/**
 * @return if the method contains a constant with the given value.
 */
fun Method.containsConstantInstructionValue(constantValue: Long): Boolean {
    return indexOfFirstConstantInstructionValue(constantValue) >= 0
}

/**
 * traverse the class hierarchy starting from the given root class
 *
 * @param targetClass the class to start traversing the class hierarchy from
 * @param callback function that is called for every class in the hierarchy
 */
fun BytecodeContext.traverseClassHierarchy(targetClass: MutableClass, callback: MutableClass.() -> Unit) {
    callback(targetClass)
    this.findClass(targetClass.superclass ?: return)?.mutableClass?.let {
        traverseClassHierarchy(it, callback)
    }
}