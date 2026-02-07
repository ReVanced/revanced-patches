package app.revanced.util

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableClassDef
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableField
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableField.Companion.toMutable
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.*
import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcode.*
import com.android.tools.smali.dexlib2.analysis.reflection.util.ReflectionUtils
import com.android.tools.smali.dexlib2.formatter.DexFormatter
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.MethodParameter
import com.android.tools.smali.dexlib2.iface.instruction.*
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.Reference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.value.*
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.value.*
import java.util.*
import kotlin.apply
import kotlin.collections.remove

/**
 * Find the instruction index used for a toString() StringBuilder write of a given String name.
 *
 * @param fieldName The name of the field to find. Partial matches are allowed.
 */
private fun Method.findInstructionIndexFromToString(fieldName: String): Int {
    val stringIndex = indexOfFirstInstruction {
        val reference = getReference<StringReference>()
        reference?.string?.contains(fieldName) == true
    }
    if (stringIndex < 0) {
        throw IllegalArgumentException("Could not find usage of string: '$fieldName'")
    }
    val stringRegister = getInstruction<OneRegisterInstruction>(stringIndex).registerA

    // Find use of the string with a StringBuilder.
    val stringUsageIndex = indexOfFirstInstruction(stringIndex) {
        val reference = getReference<MethodReference>()
        reference?.definingClass == "Ljava/lang/StringBuilder;" &&
                (this as? FiveRegisterInstruction)?.registerD == stringRegister
    }
    if (stringUsageIndex < 0) {
        throw IllegalArgumentException("Could not find StringBuilder usage in: $this")
    }

    // Find the next usage of StringBuilder, which should be the desired field.
    val fieldUsageIndex = indexOfFirstInstruction(stringUsageIndex + 1) {
        val reference = getReference<MethodReference>()
        reference?.definingClass == "Ljava/lang/StringBuilder;" && reference.name == "append"
    }
    if (fieldUsageIndex < 0) {
        // Should never happen.
        throw IllegalArgumentException("Could not find StringBuilder append usage in: $this")
    }
    val fieldUsageRegister = getInstruction<FiveRegisterInstruction>(fieldUsageIndex).registerD

    // Look backwards up the method to find the instruction that sets the register.
    var fieldSetIndex = indexOfFirstInstructionReversedOrThrow(fieldUsageIndex - 1) {
        fieldUsageRegister == writeRegister
    }

    // If the field is a method call, then adjust from MOVE_RESULT to the method call.
    val fieldSetOpcode = getInstruction(fieldSetIndex).opcode
    if (fieldSetOpcode == MOVE_RESULT ||
        fieldSetOpcode == MOVE_RESULT_WIDE ||
        fieldSetOpcode == MOVE_RESULT_OBJECT
    ) {
        fieldSetIndex--
    }

    return fieldSetIndex
}

/**
 * Find the method used for a toString() StringBuilder write of a given String name.
 *
 * @param fieldName The name of the field to find. Partial matches are allowed.
 */
context(context: BytecodePatchContext)
internal fun Method.findMethodFromToString(fieldName: String): MutableMethod {
    val methodUsageIndex = findInstructionIndexFromToString(fieldName)
    return context.navigate(this).to(methodUsageIndex).stop()
}

/**
 * Find the field used for a toString() StringBuilder write of a given String name.
 *
 * @param fieldName The name of the field to find. Partial matches are allowed.
 */
internal fun Method.findFieldFromToString(fieldName: String): FieldReference {
    val methodUsageIndex = findInstructionIndexFromToString(fieldName)
    return getInstruction<ReferenceInstruction>(methodUsageIndex).getReference<FieldReference>()!!
}

/**
 * Adds public [AccessFlags] and removes private and protected flags (if present).
 */
internal fun Int.toPublicAccessFlags(): Int = this.or(AccessFlags.PUBLIC.value)
    .and(AccessFlags.PROTECTED.value.inv())
    .and(AccessFlags.PRIVATE.value.inv())

/**
 * Apply a transform to all methods of the class.
 *
 * @param transform The transformation function. Accepts a [MutableMethod] and returns a transformed [MutableMethod].
 */
fun MutableClassDef.transformMethods(transform: MutableMethod.() -> MutableMethod) {
    val transformedMethods = methods.map { it.transform() }
    methods.clear()
    methods.addAll(transformedMethods)
}

/**
 * Inject a call to a method that hides a view.
 *
 * @param insertIndex The index to insert the call at.
 * @param viewRegister The register of the view to hide.
 * @param classDescriptor The descriptor of the class that contains the method.
 * @param targetMethod The name of the method to call.
 */
fun MutableMethod.injectHideViewCall(
    insertIndex: Int,
    viewRegister: Int,
    classDescriptor: String,
    targetMethod: String,
) = addInstruction(
    insertIndex,
    "invoke-static { v$viewRegister }, $classDescriptor->$targetMethod(Landroid/view/View;)V",
)

/**
 * Inserts instructions at a given index, using the existing control flow label at that index.
 * Inserted instructions can have it's own control flow labels as well.
 *
 * Effectively this changes the code from:
 * :label
 * (original code)
 *
 * Into:
 * :label
 * (patch code)
 * (original code)
 */
fun MutableMethod.addInstructionsAtControlFlowLabel(
    insertIndex: Int,
    instructions: String,
    vararg externalLabels: ExternalLabel,
) {
    // Duplicate original instruction and add to +1 index.
    addInstruction(insertIndex + 1, getInstruction(insertIndex))

    // Add patch code at same index as duplicated instruction,
    // so it uses the original instruction control flow label.
    addInstructionsWithLabels(insertIndex + 1, instructions, *externalLabels)

    // Remove original non duplicated instruction.
    removeInstruction(insertIndex)

    // Original instruction is now after the inserted patch instructions,
    // and the original control flow label is on the first instruction of the patch code.
}

/**
 * Get the index of the first instruction with the id of the given resource id name.
 *
 * Requires [resourceMappingPatch] as a dependency.
 *
 * @param resourceName the name of the resource to find the id for.
 * @return the index of the first instruction with the id of the given resource name, or -1 if not found.
 * @throws PatchException if the resource cannot be found.
 * @see [indexOfFirstResourceIdOrThrow], [indexOfFirstLiteralInstructionReversed]
 */
fun Method.indexOfFirstResourceId(resourceName: String): Int =
    indexOfFirstLiteralInstruction(ResourceType.ID[resourceName])

/**
 * Get the index of the first instruction with the id of the given resource name or throw a [PatchException].
 *
 * Requires [resourceMappingPatch] as a dependency.
 *
 * @throws [PatchException] if the resource is not found, or the method does not contain the resource id literal value.
 * @see [indexOfFirstResourceId], [indexOfFirstLiteralInstructionReversedOrThrow]
 */
fun Method.indexOfFirstResourceIdOrThrow(resourceName: String): Int {
    val index = indexOfFirstResourceId(resourceName)
    if (index < 0) {
        throw PatchException("Found resource id for: '$resourceName' but method does not contain the id: $this")
    }

    return index
}

/**
 * Find the index of the first literal instruction with the given long value.
 *
 * @return the first literal instruction with the value, or -1 if not found.
 * @see indexOfFirstLiteralInstructionOrThrow
 */
fun Method.indexOfFirstLiteralInstruction(literal: Long) = implementation?.let {
    it.instructions.indexOfFirst { instruction ->
        (instruction as? WideLiteralInstruction)?.wideLiteral == literal
    }
} ?: -1

/**
 * Find the index of the first literal instruction with the given long value,
 * or throw an exception if not found.
 *
 * @return the first literal instruction with the value, or throws [PatchException] if not found.
 */
fun Method.indexOfFirstLiteralInstructionOrThrow(literal: Long): Int {
    val index = indexOfFirstLiteralInstruction(literal)
    if (index < 0) throw PatchException("Could not find long literal: $literal")
    return index
}

/**
 * Find the index of the first literal instruction with the given float value.
 *
 * @return the first literal instruction with the value, or -1 if not found.
 * @see indexOfFirstLiteralInstructionOrThrow
 */
fun Method.indexOfFirstLiteralInstruction(literal: Float) = indexOfFirstLiteralInstruction(literal.toRawBits().toLong())

/**
 * Find the index of the first literal instruction with the given float value,
 * or throw an exception if not found.
 *
 * @return the first literal instruction with the value, or throws [PatchException] if not found.
 */
fun Method.indexOfFirstLiteralInstructionOrThrow(literal: Float): Int {
    val index = indexOfFirstLiteralInstruction(literal)
    if (index < 0) throw PatchException("Could not find float literal: $literal")
    return index
}

/**
 * Find the index of the first literal instruction with the given double value.
 *
 * @return the first literal instruction with the value, or -1 if not found.
 * @see indexOfFirstLiteralInstructionOrThrow
 */
fun Method.indexOfFirstLiteralInstruction(literal: Double) = indexOfFirstLiteralInstruction(literal.toRawBits())

/**
 * Find the index of the first literal instruction with the given double value,
 * or throw an exception if not found.
 *
 * @return the first literal instruction with the value, or throws [PatchException] if not found.
 */
fun Method.indexOfFirstLiteralInstructionOrThrow(literal: Double): Int {
    val index = indexOfFirstLiteralInstruction(literal)
    if (index < 0) throw PatchException("Could not find double literal: $literal")
    return index
}

/**
 * Find the index of the last literal instruction with the given value.
 *
 * @return the last literal instruction with the value, or -1 if not found.
 * @see indexOfFirstLiteralInstructionOrThrow
 */
fun Method.indexOfFirstLiteralInstructionReversed(literal: Long) = implementation?.let {
    it.instructions.indexOfLast { instruction ->
        (instruction as? WideLiteralInstruction)?.wideLiteral == literal
    }
} ?: -1

/**
 * Find the index of the last wide literal instruction with the given long value,
 * or throw an exception if not found.
 *
 * @return the last literal instruction with the value, or throws [PatchException] if not found.
 */
fun Method.indexOfFirstLiteralInstructionReversedOrThrow(literal: Long): Int {
    val index = indexOfFirstLiteralInstructionReversed(literal)
    if (index < 0) throw PatchException("Could not find long literal: $literal")
    return index
}

/**
 * Find the index of the last literal instruction with the given float value.
 *
 * @return the last literal instruction with the value, or -1 if not found.
 * @see indexOfFirstLiteralInstructionOrThrow
 */
fun Method.indexOfFirstLiteralInstructionReversed(literal: Float) =
    indexOfFirstLiteralInstructionReversed(literal.toRawBits().toLong())

/**
 * Find the index of the last wide literal instruction with the given float value,
 * or throw an exception if not found.
 *
 * @return the last literal instruction with the value, or throws [PatchException] if not found.
 */
fun Method.indexOfFirstLiteralInstructionReversedOrThrow(literal: Float): Int {
    val index = indexOfFirstLiteralInstructionReversed(literal)
    if (index < 0) throw PatchException("Could not find float literal: $literal")
    return index
}

/**
 * Find the index of the last literal instruction with the given double value.
 *
 * @return the last literal instruction with the value, or -1 if not found.
 * @see indexOfFirstLiteralInstructionOrThrow
 */
fun Method.indexOfFirstLiteralInstructionReversed(literal: Double) =
    indexOfFirstLiteralInstructionReversed(literal.toRawBits())

/**
 * Find the index of the last wide literal instruction with the given double value,
 * or throw an exception if not found.
 *
 * @return the last literal instruction with the value, or throws [PatchException] if not found.
 */
fun Method.indexOfFirstLiteralInstructionReversedOrThrow(literal: Double): Int {
    val index = indexOfFirstLiteralInstructionReversed(literal)
    if (index < 0) throw PatchException("Could not find double literal: $literal")
    return index
}

/**
 * Check if the method contains a literal with the given long value.
 *
 * @return if the method contains a literal with the given value.
 */
fun Method.containsLiteralInstruction(literal: Long) = indexOfFirstLiteralInstruction(literal) >= 0

/**
 * Check if the method contains a literal with the given float value.
 *
 * @return if the method contains a literal with the given value.
 */
fun Method.containsLiteralInstruction(literal: Float) = indexOfFirstLiteralInstruction(literal) >= 0

/**
 * Check if the method contains a literal with the given double value.
 *
 * @return if the method contains a literal with the given value.
 */
fun Method.containsLiteralInstruction(literal: Double) = indexOfFirstLiteralInstruction(literal) >= 0

/**
 * Traverse the class hierarchy starting from the given root class.
 *
 * @param targetClass the class to start traversing the class hierarchy from.
 * @param callback function that is called for every class in the hierarchy.
 */
fun BytecodePatchContext.traverseClassHierarchy(targetClass: MutableClassDef, callback: MutableClassDef.() -> Unit) {
    callback(targetClass)

    targetClass.superclass ?: return

    firstClassDefOrNull(targetClass.superclass!!)?.let {
        traverseClassHierarchy(it, callback)
    }
}

/**
 * Get the [Reference] of an [Instruction] as [T].
 *
 * @param T The type of [Reference] to cast to.
 * @return The [Reference] as [T] or null
 * if the [Instruction] is not a [ReferenceInstruction] or the [Reference] is not of type [T].
 * @see ReferenceInstruction
 */
inline fun <reified T : Reference> Instruction.getReference() = (this as? ReferenceInstruction)?.reference as? T

/**
 * @return The index of the first opcode specified, or -1 if not found.
 * @see indexOfFirstInstructionOrThrow
 */
fun Method.indexOfFirstInstruction(targetOpcode: Opcode): Int = indexOfFirstInstruction(0, targetOpcode)

/**
 * @param startIndex Optional starting index to start searching from.
 * @return The index of the first opcode specified, or -1 if not found.
 * @see indexOfFirstInstructionOrThrow
 */
fun Method.indexOfFirstInstruction(startIndex: Int = 0, targetOpcode: Opcode): Int =
    indexOfFirstInstruction(startIndex) {
        opcode == targetOpcode
    }

/**
 * Get the index of the first [Instruction] that matches the predicate, starting from [startIndex].
 *
 * @param startIndex Optional starting index to start searching from.
 * @return -1 if the instruction is not found.
 * @see indexOfFirstInstructionOrThrow
 */
fun Method.indexOfFirstInstruction(startIndex: Int = 0, filter: Instruction.() -> Boolean): Int {
    var instructions = this.implementation?.instructions ?: return -1
    if (startIndex != 0) {
        instructions = instructions.drop(startIndex)
    }
    val index = instructions.indexOfFirst(filter)

    return if (index >= 0) {
        startIndex + index
    } else {
        -1
    }
}

/**
 * @return The index of the first opcode specified
 * @throws PatchException
 * @see indexOfFirstInstruction
 */
fun Method.indexOfFirstInstructionOrThrow(targetOpcode: Opcode): Int = indexOfFirstInstructionOrThrow(0, targetOpcode)

/**
 * @return The index of the first opcode specified, starting from the index specified.
 * @throws PatchException
 * @see indexOfFirstInstruction
 */
fun Method.indexOfFirstInstructionOrThrow(startIndex: Int = 0, targetOpcode: Opcode): Int =
    indexOfFirstInstructionOrThrow(startIndex) {
        opcode == targetOpcode
    }

/**
 * Get the index of the first [Instruction] that matches the predicate, starting from [startIndex].
 *
 * @return The index of the instruction.
 * @throws PatchException
 * @see indexOfFirstInstruction
 */
fun Method.indexOfFirstInstructionOrThrow(startIndex: Int = 0, filter: Instruction.() -> Boolean): Int {
    val index = indexOfFirstInstruction(startIndex, filter)
    if (index < 0) {
        throw PatchException("Could not find instruction index")
    }

    return index
}

/**
 * Get the index of matching instruction,
 * starting from and [startIndex] and searching down.
 *
 * @param startIndex Optional starting index to search down from. Searching includes the start index.
 * @return -1 if the instruction is not found.
 * @see indexOfFirstInstructionReversedOrThrow
 */
fun Method.indexOfFirstInstructionReversed(startIndex: Int? = null, targetOpcode: Opcode): Int =
    indexOfFirstInstructionReversed(startIndex) {
        opcode == targetOpcode
    }

/**
 * Get the index of matching instruction,
 * starting from and [startIndex] and searching down.
 *
 * @param startIndex Optional starting index to search down from. Searching includes the start index.
 * @return -1 if the instruction is not found.
 * @see indexOfFirstInstructionReversedOrThrow
 */
fun Method.indexOfFirstInstructionReversed(startIndex: Int? = null, filter: Instruction.() -> Boolean): Int {
    var instructions = this.implementation?.instructions ?: return -1
    if (startIndex != null) {
        instructions = instructions.take(startIndex + 1)
    }

    return instructions.indexOfLast(filter)
}

/**
 * Get the index of matching instruction,
 * starting from the end of the method and searching down.
 *
 * @return -1 if the instruction is not found.
 */
fun Method.indexOfFirstInstructionReversed(targetOpcode: Opcode): Int = indexOfFirstInstructionReversed {
    opcode == targetOpcode
}

/**
 * Get the index of matching instruction,
 * starting from [startIndex] and searching down.
 *
 * @param startIndex Optional starting index to search down from. Searching includes the start index.
 * @return The index of the instruction.
 * @see indexOfFirstInstructionReversed
 */
fun Method.indexOfFirstInstructionReversedOrThrow(startIndex: Int? = null, targetOpcode: Opcode): Int =
    indexOfFirstInstructionReversedOrThrow(startIndex) {
        opcode == targetOpcode
    }

/**
 * Get the index of matching instruction,
 * starting from the end of the method and searching down.
 *
 * @return -1 if the instruction is not found.
 */
fun Method.indexOfFirstInstructionReversedOrThrow(targetOpcode: Opcode): Int = indexOfFirstInstructionReversedOrThrow {
    opcode == targetOpcode
}

/**
 * Get the index of matching instruction,
 * starting from [startIndex] and searching down.
 *
 * @param startIndex Optional starting index to search down from. Searching includes the start index.
 * @return The index of the instruction.
 * @see indexOfFirstInstructionReversed
 */
fun Method.indexOfFirstInstructionReversedOrThrow(startIndex: Int? = null, filter: Instruction.() -> Boolean): Int {
    val index = indexOfFirstInstructionReversed(startIndex, filter)

    if (index < 0) {
        throw PatchException("Could not find instruction index")
    }

    return index
}

/**
 * @return An immutable list of indices of the instructions in reverse order.
 *  _Returns an empty list if no indices are found_
 *  @see findInstructionIndicesReversedOrThrow
 */
fun Method.findInstructionIndicesReversed(filter: Instruction.() -> Boolean): List<Int> = instructions
    .withIndex()
    .filter { (_, instruction) -> filter(instruction) }
    .map { (index, _) -> index }
    .asReversed()

/**
 * @return An immutable list of indices of the instructions in reverse order.
 * @throws PatchException if no matching indices are found.
 */
fun Method.findInstructionIndicesReversedOrThrow(filter: Instruction.() -> Boolean): List<Int> {
    val indexes = findInstructionIndicesReversed(filter)
    if (indexes.isEmpty()) throw PatchException("No matching instructions found in: $this")

    return indexes
}

/**
 * @return An immutable list of indices of the opcode in reverse order.
 *  _Returns an empty list if no indices are found_
 * @see findInstructionIndicesReversedOrThrow
 */
fun Method.findInstructionIndicesReversed(opcode: Opcode): List<Int> =
    findInstructionIndicesReversed { this.opcode == opcode }

/**
 * @return An immutable list of indices of the opcode in reverse order.
 * @throws PatchException if no matching indices are found.
 */
fun Method.findInstructionIndicesReversedOrThrow(opcode: Opcode): List<Int> {
    val instructions = findInstructionIndicesReversed(opcode)
    if (instructions.isEmpty()) throw PatchException("Could not find opcode: $opcode in: $this")

    return instructions
}

/**
 * Overrides the first move result with an extension call.
 * Suitable for calls to extension code to override boolean and integer values.
 */
internal fun MutableMethod.insertLiteralOverride(literal: Long, extensionMethodDescriptor: String) {
    val literalIndex = indexOfFirstLiteralInstructionOrThrow(literal)
    insertLiteralOverride(literalIndex, extensionMethodDescriptor)
}

internal fun MutableMethod.insertLiteralOverride(literalIndex: Int, extensionMethodDescriptor: String) {
    // TODO: make this work with objects and wide primitive values.
    val index = indexOfFirstInstructionOrThrow(literalIndex, MOVE_RESULT)
    val register = getInstruction<OneRegisterInstruction>(index).registerA

    val operation = if (register < 16) {
        "invoke-static { v$register }"
    } else {
        "invoke-static/range { v$register .. v$register }"
    }

    addInstructions(
        index + 1,
        """
            $operation, $extensionMethodDescriptor
            move-result v$register
        """,
    )
}

/**
 * Overrides a literal value result with a constant value.
 */
internal fun MutableMethod.insertLiteralOverride(literal: Long, override: Boolean) {
    val literalIndex = indexOfFirstLiteralInstructionOrThrow(literal)
    return insertLiteralOverride(literalIndex, override)
}

/**
 * Constant value override of the first MOVE_RESULT after the index parameter.
 */
internal fun MutableMethod.insertLiteralOverride(literalIndex: Int, override: Boolean) {
    val index = indexOfFirstInstructionOrThrow(literalIndex, MOVE_RESULT)
    val register = getInstruction<OneRegisterInstruction>(index).registerA
    val overrideValue = if (override) "0x1" else "0x0"

    addInstruction(
        index + 1,
        "const v$register, $overrideValue",
    )
}

/**
 * Iterates all instructions as sequence in all methods of all classes in the [BytecodePatchContext].
 *
 * @param match A function that matches instructions. If a match is found, it returns a value of type [T], otherwise null.
 * @param transform A function that transforms the matched instruction using the mutable method and the matched value
 * of type [T].
 */
fun <T> BytecodePatchContext.forEachInstructionAsSequence(
    match: (classDef: ClassDef, method: Method, instruction: Instruction, index: Int) -> T?,
    transform: (MutableMethod, T) -> Unit
) {
    classDefs.flatMap { classDef ->
        classDef.methods.mapNotNull { method ->
            val matches = method.instructionsOrNull?.mapIndexedNotNull { index, instruction ->
                match(classDef, method, instruction, index)
            } ?: return@mapNotNull null

            if (matches.any()) method to matches else null
        }
    }.forEach { (method, matches) ->

        val method = firstMethod(method)
        val matches = matches.toCollection(ArrayDeque())

        while (!matches.isEmpty()) transform(method, matches.removeLast())
    }
}

private fun MutableMethod.checkReturnType(expectedTypes: Iterable<Class<*>>) {
    val returnTypeJava = ReflectionUtils.dexToJavaName(returnType)
    check(expectedTypes.any { returnTypeJava == it.name }) {
        "Actual return type $returnTypeJava is not contained in expected types: $expectedTypes"
    }

}

/**
 * Additional registers effectively take the place of the pX parameters (p0, p1, p2, etc.)
 * and contain the original contents of the method parameters.
 * Added registers always start at index: `originalMethod.implementation!!.registerCount` of the
 * original uncloned method.
 *
 * **Fingerprint match indexes will be increased positively by [numberOfParameterRegistersLogical]**.
 */
context(_: BytecodePatchContext)
fun Method.cloneMutableAndPreserveParameters() =
    cloneMutableAndPreserveParameters(classDef)

/**
 * Additional registers effectively take the place of the pX parameters (p0, p1, p2, etc.)
 * and contain the original contents of the method parameters.
 * Added registers always start at index: `originalMethod.implementation!!.registerCount` of the
 * original uncloned method.
 *
 * **Fingerprint match indexes will be increased positively by [numberOfParameterRegistersLogical]**.
 */
fun Method.cloneMutableAndPreserveParameters(mutableClassDef : MutableClassDef) : MutableMethod {
    check (!AccessFlags.STATIC.isSet(accessFlags) || parameters.isNotEmpty()) {
        "Static methods have no parameter registers to preserve"
    }

    val clonedMethod = cloneMutable(
        additionalRegisters = numberOfParameterRegisters
    )

    // Replace existing method with cloned with more registers.
    mutableClassDef.methods.apply {
        remove(this@cloneMutableAndPreserveParameters)
        add(clonedMethod)
    }

    return clonedMethod
}

// Adapted from BiliRoamingX:
// https://github.com/BiliRoamingX/BiliRoamingX/blob/ae58109f3acdd53ec2d2b3fb439c2a2ef1886221/patches/src/main/kotlin/app/revanced/patches/bilibili/utils/Extenstions.kt#L51
/**
 * Additional registers effectively take the place of the pX parameters (p0, p1, p2, etc.)
 * and contain the original contents of the method parameters.
 * Added registers always start at index: `originalMethod.implementation!!.registerCount` of the
 * original uncloned method.
 *
 * **Fingerprint match indexes will be increased positively by [additionalRegisters]**.
 */
fun Method.cloneMutable(
    name: String = this.name,
    accessFlags: Int = this.accessFlags,
    parameters: List<MethodParameter> = this.parameters,
    returnType: String = this.returnType,
    additionalRegisters: Int = 0,
): MutableMethod {
    check(additionalRegisters >= 0) {
        "Additional registers cannot be negative"
    }

    val implementationExists = implementation != null
    val oldFirstParameterRegister = if (implementationExists) p0Register else 0

    val clonedImplementation = implementation?.let {
        ImmutableMethodImplementation(
            it.registerCount + additionalRegisters,
            it.instructions,
            it.tryBlocks,
            it.debugItems,
        )
    }

    return ImmutableMethod(
        definingClass,
        name,
        parameters,
        returnType,
        accessFlags,
        annotations,
        hiddenApiRestrictions,
        clonedImplementation
    ).toMutable().apply {
        var insertIndex = 0
        var addedInstructions = 0
        val isNotStatic = !AccessFlags.STATIC.isSet(accessFlags)

        if (implementationExists && additionalRegisters > 0 && (parameters.isNotEmpty() || isNotStatic)) {
            var destReg = oldFirstParameterRegister
            var pReg = 0

            // Handle `this`.
            if (isNotStatic) {
                addInstructions(insertIndex++, "move-object/from16 v$destReg, p$pReg")
                addedInstructions++
                destReg += 1
                pReg += 1
            }

            // Handle method parameters.
            for (parameter in parameters) {
                val opcode = when (parameter.type) {
                    "J", "D" -> "move-wide/from16"
                    else -> {
                        if (parameter.type.startsWith('L') || parameter.type.startsWith('[')) {
                            "move-object/from16"
                        } else {
                            "move/from16"
                        }
                    }
                }

                addInstructions(insertIndex++, "$opcode v$destReg, p$pReg")
                addedInstructions++

                val width = if (opcode.startsWith("move-wide")) 2 else 1
                destReg += width
                pReg += width
            }

            if (addedInstructions != numberOfParameterRegistersLogical) {
                throw IllegalStateException(
                    "Added instructions do not match additional registers " +
                            "addedInstructions: $addedInstructions " +
                            "numberOfParameterRegistersLogical: $numberOfParameterRegistersLogical"
                )
            }
        }
    }
}

/**
 * @return The number of registers for all parameters, including p0.
 * This includes 2 registers for each wide parameter.
 */
val Method.numberOfParameterRegisters: Int
    get() {
        var count = 0

        if (!AccessFlags.STATIC.isSet(accessFlags)) {
            count += 1
        }

        for (param in parameters) {
            count += when (param.type) {
                "J", "D" -> 2   // wide
                else -> 1       // normal
            }
        }

        return count
    }

/**
 * @return The number of parameter registers, including p0 as 'this' if method is not static.
 *   This differs from [numberOfParameterRegisters] in that long/double parameters are counted only once each.
 */
val Method.numberOfParameterRegistersLogical: Int
    get() = parameters.count() + if (AccessFlags.STATIC.isSet(accessFlags)) {
        0
    } else {
        1
    }

/**
 * @return the actual register number of p0 for this method.
 * Throws if the method has no implementation.
 */
val Method.p0Register: Int
    get() {
        val impl = implementation ?: throw IllegalStateException("Method has no implementation: $this")
        var paramRegs = 0

        // Count explicit parameters (wide types take 2 registers).
        for (type in this.parameterTypes) {
            paramRegs += if (type == "J" || type == "D") 2 else 1
        }

        // Add implicit 'this' for non-static methods.
        if (!AccessFlags.STATIC.isSet(this.accessFlags)) {
            paramRegs += 1
        }

        val totalRegs = impl.registerCount

        return totalRegs - paramRegs
    }

private const val RETURN_TYPE_MISMATCH = "Mismatch between override type and Method return type"

/**
 * Overrides the first instruction of a method with returning the default value for the type (or `void`).
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly() {
    val value = when (returnType) {
        "V" -> null
        "Z" -> ImmutableBooleanEncodedValue.FALSE_VALUE
        "B" -> ImmutableByteEncodedValue(0)
        "S" -> ImmutableShortEncodedValue(0)
        "C" -> ImmutableCharEncodedValue(Char.MIN_VALUE)
        "I" -> ImmutableIntEncodedValue(0)
        "F" -> ImmutableFloatEncodedValue(0f)
        "J" -> ImmutableLongEncodedValue(0)
        "D" -> ImmutableDoubleEncodedValue(0.0)
        else -> ImmutableNullEncodedValue.INSTANCE
    }
    overrideReturnValue(value, false)
}

private fun MutableMethod.returnString(value: String, late: Boolean) {
    checkReturnType(String::class.java.allAssignableTypes())
    overrideReturnValue(ImmutableStringEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `String` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: String) = returnString(value, false)

/**
 * Overrides all return statements with a constant `String` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: String) = returnString(value, true)

private fun MutableMethod.returnByte(value: Byte, late: Boolean) {
    checkReturnType(Byte::class.javaObjectType.allAssignableTypes() + Byte::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableByteEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Byte` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Byte) = returnByte(value, false)

/**
 * Overrides all return statements with a constant `Byte` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Byte) = returnByte(value, true)

private fun MutableMethod.returnBoolean(value: Boolean, late: Boolean) {
    checkReturnType(Boolean::class.javaObjectType.allAssignableTypes() + Boolean::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableBooleanEncodedValue.forBoolean(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Boolean` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Boolean) = returnBoolean(value, false)

/**
 * Overrides all return statements with a constant `Boolean` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Boolean) = returnBoolean(value, true)

private fun MutableMethod.returnShort(value: Short, late: Boolean) {
    checkReturnType(Short::class.javaObjectType.allAssignableTypes() + Short::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableShortEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Short` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Short) = returnShort(value, false)

/**
 * Overrides all return statements with a constant `Short` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Short) = returnShort(value, true)

private fun MutableMethod.returnChar(value: Char, late: Boolean) {
    checkReturnType(Char::class.javaObjectType.allAssignableTypes() + Char::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableCharEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Char` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Char) = returnChar(value, false)

/**
 * Overrides all return statements with a constant `Char` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Char) = returnChar(value, true)

private fun MutableMethod.returnInt(value: Int, late: Boolean) {
    checkReturnType(Int::class.javaObjectType.allAssignableTypes() + Int::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableIntEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Int` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Int) = returnInt(value, false)

/**
 * Overrides all return statements with a constant `Int` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Int) = returnInt(value, true)

private fun MutableMethod.returnFloat(value: Float, late: Boolean) {
    checkReturnType(Float::class.javaObjectType.allAssignableTypes() + Float::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableFloatEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Float` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Float) = returnFloat(value, false)

/**
 * Overrides all return statements with a constant `Float` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Float) = returnFloat(value, true)

private fun MutableMethod.returnLong(value: Long, late: Boolean) {
    checkReturnType(Long::class.javaObjectType.allAssignableTypes() + Long::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableLongEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Long` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Long) = returnLong(value, false)

/**
 * Overrides all return statements with a constant `Long` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Long) = returnLong(value, true)

private fun MutableMethod.returnDouble(value: Double, late: Boolean) {
    checkReturnType(Double::class.javaObjectType.allAssignableTypes() + Double::class.javaPrimitiveType!!)
    overrideReturnValue(ImmutableDoubleEncodedValue(value), late)
}

/**
 * Overrides the first instruction of a method with a constant `Double` return value.
 * None of the method code will ever execute.
 *
 * @see returnLate
 */
fun MutableMethod.returnEarly(value: Double) = returnDouble(value, false)

/**
 * Overrides all return statements with a constant `Double` value.
 * All method code is executed the same as unpatched.
 *
 * @see returnEarly
 */
fun MutableMethod.returnLate(value: Double) = returnDouble(value, true)

private fun MutableMethod.overrideReturnValue(value: EncodedValue?, returnLate: Boolean) {
    val instructions = if (value == null) {
        require(!returnLate) {
            "Cannot return late for method of void type"
        }
        "return-void"
    } else {
        val encodedValue = DexFormatter.INSTANCE.getEncodedValue(value)
        when (value) {
            is NullEncodedValue -> {
                """
                const/4 v0, 0x0
                return-object v0
                """
            }

            is StringEncodedValue -> {
                """
                const-string v0, $encodedValue
                return-object v0
                """
            }

            is ByteEncodedValue -> {
                if (returnType == "B") {
                    """
                    const/4 v0, $encodedValue
                    return v0
                    """
                } else {
                    """
                    const/4 v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Byte;->valueOf(B)Ljava/lang/Byte;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            is BooleanEncodedValue -> {
                val encodedValue = value.value.toHexString()
                if (returnType == "Z") {
                    """
                    const/4 v0, $encodedValue
                    return v0
                    """
                } else {
                    """
                    const/4 v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            is ShortEncodedValue -> {
                if (returnType == "S") {
                    """
                    const/16 v0, $encodedValue
                    return v0
                    """
                } else {
                    """
                    const/16 v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Short;->valueOf(S)Ljava/lang/Short;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            is CharEncodedValue -> {
                if (returnType == "C") {
                    """
                    const/16 v0, $encodedValue
                    return v0
                    """
                } else {
                    """
                    const/16 v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Character;->valueOf(C)Ljava/lang/Character;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            is IntEncodedValue -> {
                if (returnType == "I") {
                    """
                    const v0, $encodedValue
                    return v0
                    """
                } else {
                    """
                    const v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            is FloatEncodedValue -> {
                val encodedValue = "${encodedValue}f"
                if (returnType == "F") {
                    """
                    const v0, $encodedValue
                    return v0
                    """
                } else {
                    """
                    const v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            is LongEncodedValue -> {
                val encodedValue = "${encodedValue}L"
                if (returnType == "J") {
                    """
                    const-wide v0, $encodedValue
                    return-wide v0
                    """
                } else {
                    """
                    const-wide v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            is DoubleEncodedValue -> {
                if (returnType == "D") {
                    """
                    const-wide v0, $encodedValue
                    return-wide v0
                    """
                } else {
                    """
                    const-wide v0, $encodedValue
                    invoke-static { v0 }, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;
                    move-result-object v0
                    return-object v0
                    """
                }
            }

            else -> throw IllegalArgumentException("Value $value cannot be returned from $this")
        }
    }

    if (returnLate) {
        findInstructionIndicesReversedOrThrow {
            opcode == RETURN || opcode == RETURN_WIDE || opcode == RETURN_OBJECT
        }.forEach { index ->
            addInstructionsAtControlFlowLabel(index, instructions)
        }
    } else {
        addInstructions(0, instructions)
    }
}

/**
 * Remove the given AccessFlags from the field.
 */
internal fun MutableField.removeFlags(vararg flags: AccessFlags) {
    val bitField = flags.map { it.value }.reduce { acc, flag -> acc and flag }
    this.accessFlags = this.accessFlags and bitField.inv()
}

internal fun BytecodePatchContext.addStaticFieldToExtension(
    type: String,
    methodName: String,
    fieldName: String,
    objectClass: String,
    smaliInstructions: String,
) {
    val mutableClass = firstClassDef(type)
    val objectCall = "$mutableClass->$fieldName:$objectClass"

    mutableClass.apply {
        methods.first { method -> method.name == methodName }.apply {
            staticFields.add(
                ImmutableField(
                    definingClass,
                    fieldName,
                    objectClass,
                    AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
                    null,
                    annotations,
                    null,
                ).toMutable(),
            )

            addInstructionsWithLabels(
                0,
                """
                    sget-object v0, $objectCall
                """ + smaliInstructions,
            )
        }
    }
}

/**
 * Set the custom condition for this predicate to check for a literal value.
 *
 * @param literalSupplier The supplier for the literal value to check for.
 */
@Deprecated("Instead use `literal()`")
fun MutablePredicateList<Method>.literal(literalSupplier: () -> Long) {
    custom { containsLiteralInstruction(literalSupplier()) }
}

