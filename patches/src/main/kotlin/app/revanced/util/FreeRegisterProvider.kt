package app.revanced.util

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.instructions
import app.revanced.util.FreeRegisterProvider.Companion.branchOpcodes
import app.revanced.util.FreeRegisterProvider.Companion.conditionalBranchOpcodes
import app.revanced.util.FreeRegisterProvider.Companion.returnOpcodes
import app.revanced.util.FreeRegisterProvider.Companion.writeOpcodes
import com.android.tools.smali.dexlib2.Format
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcode.*
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OffsetInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import java.util.EnumSet
import java.util.LinkedList
import java.util.logging.Logger

/**
 * Finds free registers at a specific index in a method.
 * Allows allocating multiple free registers for a given index.
 *
 * If you only need a single free register, instead use [findFreeRegister].
 *
 * @throws IllegalArgumentException If no free registers can be found at the given index.
 *                                  This includes unusual method indexes that read from every register
 *                                  before any registers are wrote to, or if a switch statement is
 *                                  encountered before any free registers are found.
 */
fun Method.getFreeRegisterProvider(startIndex: Int, registersToExclude: List<Int>) =
    FreeRegisterProvider(this, startIndex, registersToExclude)

/**
 * Finds free registers at a specific index in a method.
 * Allows allocating multiple free registers for a given index.
 *
 * If you only need a single free register, instead use [findFreeRegister].
 *
 * @throws IllegalArgumentException If no free registers can be found at the given index.
 *                                  This includes unusual method indexes that read from every register
 *                                  before any registers are wrote to, or if a switch statement is
 *                                  encountered before any free registers are found.
 */
fun Method.getFreeRegisterProvider(startIndex: Int, vararg registersToExclude: Int) =
    FreeRegisterProvider(this, startIndex, *registersToExclude)

/**
 * Simple wrapper around [findFreeRegister] that allows finding then allocating multiple registers.
 * If you only need a one free register, instead use [findFreeRegister].
 */
class FreeRegisterProvider internal constructor(
    method: Method,
    startIndex: Int,
    registersToExclude: List<Int>
) {

    internal constructor(
        method: Method,
        startIndex: Int,
        vararg registersToExclude: Int
    ) : this(method, startIndex, registersToExclude.toList())

    private var freeRegisters: MutableList<Int> = LinkedList(
        method.findFreeRegisters(startIndex, registersToExclude)
    )

    private val originallyExcludedRegisters = registersToExclude
    private val allocatedFreeRegisters = mutableListOf<Int>()

    /**
     * Returns a free register and removes it from the available list.
     *
     * @return A free register number
     * @throws IllegalStateException if no free registers are available
     */
    fun getFreeRegister(): Int {
        if (freeRegisters.isEmpty()) {
            throw IllegalStateException("No free registers available")
        }
        val register = freeRegisters.removeFirst()
        allocatedFreeRegisters.add(register)
        return register
    }

    /**
     * Returns all registers that have been allocated via [getFreeRegister].
     * This does not include the originally excluded registers.
     *
     * @return List of registers that have been allocated, in allocation order
     */
    fun getAllocatedFreeRegisters(): List<Int> = allocatedFreeRegisters.toList()

    /**
     * Returns all registers that are considered "in use" - both originally
     * excluded registers and newly allocated registers.
     *
     * @return List of all registers that should not be used,
     *         with originally excluded registers first, followed by
     *         newly allocated registers in allocation order
     */
    fun getUsedAndExcludedRegisters(): List<Int> =
        originallyExcludedRegisters + allocatedFreeRegisters

    /**
     * @return The number of free registers still available.
     */
    fun availableCount(): Int = freeRegisters.size

    /**
     * Checks if there are any free registers available.
     */
    fun hasFreeRegisters(): Boolean = freeRegisters.isNotEmpty()

    internal companion object {
        val conditionalBranchOpcodes: EnumSet<Opcode> = EnumSet.of(
            IF_EQ, IF_NE, IF_LT, IF_GE, IF_GT, IF_LE,
            IF_EQZ, IF_NEZ, IF_LTZ, IF_GEZ, IF_GTZ, IF_LEZ
        )

        val branchOpcodes: EnumSet<Opcode> = conditionalBranchOpcodes.clone().also {
            it.addAll(listOf(GOTO, GOTO_16, GOTO_32, PACKED_SWITCH, SPARSE_SWITCH))
        }

        val returnOpcodes: EnumSet<Opcode> = EnumSet.of(
            RETURN_VOID, RETURN, RETURN_WIDE, RETURN_OBJECT, RETURN_VOID_NO_BARRIER,
            THROW
        )

        val writeOpcodes: EnumSet<Opcode> = EnumSet.of(
            ARRAY_LENGTH,
            INSTANCE_OF,
            NEW_INSTANCE, NEW_ARRAY,
            MOVE, MOVE_FROM16, MOVE_16, MOVE_WIDE, MOVE_WIDE_FROM16, MOVE_WIDE_16, MOVE_OBJECT,
            MOVE_OBJECT_FROM16, MOVE_OBJECT_16, MOVE_RESULT, MOVE_RESULT_WIDE, MOVE_RESULT_OBJECT, MOVE_EXCEPTION,
            CONST, CONST_4, CONST_16, CONST_HIGH16, CONST_WIDE_16, CONST_WIDE_32,
            CONST_WIDE, CONST_WIDE_HIGH16, CONST_STRING, CONST_STRING_JUMBO,
            IGET, IGET_WIDE, IGET_OBJECT, IGET_BOOLEAN, IGET_BYTE, IGET_CHAR, IGET_SHORT,
            IGET_VOLATILE, IGET_WIDE_VOLATILE, IGET_OBJECT_VOLATILE,
            SGET, SGET_WIDE, SGET_OBJECT, SGET_BOOLEAN, SGET_BYTE, SGET_CHAR, SGET_SHORT,
            SGET_VOLATILE, SGET_WIDE_VOLATILE, SGET_OBJECT_VOLATILE,
            AGET, AGET_WIDE, AGET_OBJECT, AGET_BOOLEAN, AGET_BYTE, AGET_CHAR, AGET_SHORT,
            // Arithmetic and logical operations.
            ADD_DOUBLE_2ADDR, ADD_DOUBLE, ADD_FLOAT_2ADDR, ADD_FLOAT, ADD_INT_2ADDR,
            ADD_INT_LIT8, ADD_INT, ADD_LONG_2ADDR, ADD_LONG, ADD_INT_LIT16,
            AND_INT_2ADDR, AND_INT_LIT8, AND_INT_LIT16, AND_INT, AND_LONG_2ADDR, AND_LONG,
            DIV_DOUBLE_2ADDR, DIV_DOUBLE, DIV_FLOAT_2ADDR, DIV_FLOAT, DIV_INT_2ADDR,
            DIV_INT_LIT16, DIV_INT_LIT8, DIV_INT, DIV_LONG_2ADDR, DIV_LONG,
            DOUBLE_TO_FLOAT, DOUBLE_TO_INT, DOUBLE_TO_LONG,
            FLOAT_TO_DOUBLE, FLOAT_TO_INT, FLOAT_TO_LONG,
            INT_TO_BYTE, INT_TO_CHAR, INT_TO_DOUBLE, INT_TO_FLOAT, INT_TO_LONG, INT_TO_SHORT,
            LONG_TO_DOUBLE, LONG_TO_FLOAT, LONG_TO_INT,
            MUL_DOUBLE_2ADDR, MUL_DOUBLE, MUL_FLOAT_2ADDR, MUL_FLOAT, MUL_INT_2ADDR,
            MUL_INT_LIT16, MUL_INT_LIT8, MUL_INT, MUL_LONG_2ADDR, MUL_LONG,
            NEG_DOUBLE, NEG_FLOAT, NEG_INT, NEG_LONG,
            NOT_INT, NOT_LONG,
            OR_INT_2ADDR, OR_INT_LIT16, OR_INT_LIT8, OR_INT, OR_LONG_2ADDR, OR_LONG,
            REM_DOUBLE_2ADDR, REM_DOUBLE, REM_FLOAT_2ADDR, REM_FLOAT, REM_INT_2ADDR,
            REM_INT_LIT16, REM_INT_LIT8, REM_INT, REM_LONG_2ADDR, REM_LONG,
            RSUB_INT_LIT8, RSUB_INT,
            SHL_INT_2ADDR, SHL_INT_LIT8, SHL_INT, SHL_LONG_2ADDR, SHL_LONG,
            SHR_INT_2ADDR, SHR_INT_LIT8, SHR_INT, SHR_LONG_2ADDR, SHR_LONG,
            SUB_DOUBLE_2ADDR, SUB_DOUBLE, SUB_FLOAT_2ADDR, SUB_FLOAT, SUB_INT_2ADDR,
            SUB_INT, SUB_LONG_2ADDR, SUB_LONG,
            USHR_INT_2ADDR, USHR_INT_LIT8, USHR_INT, USHR_LONG_2ADDR, USHR_LONG,
            XOR_INT_2ADDR, XOR_INT_LIT16, XOR_INT_LIT8, XOR_INT, XOR_LONG_2ADDR, XOR_LONG,
        )
    }
}

/**
 * Starting from and including the instruction at index [startIndex], finds the next register
 * that is written to and not read from.
 *
 * This method can return a non 4-bit register, and the calling code may need to temporarily
 * swap register contents if a 4-bit register is required.
 *
 * @param startIndex Inclusive starting index.
 * @param registersToExclude Registers to exclude, and consider as used. For most use cases,
 *                           all registers used in injected code should be specified.
 * @return The lowest register number (usually a 4-bit register) that is free at the given index.
 * @throws IllegalArgumentException If no free registers can be found at the given index.
 *                                  This includes unusual method indexes that read from every register
 *                                  before any registers are wrote to, or if a switch statement is
 *                                  encountered before any free registers are found.
 * @see [FreeRegisterProvider]
 */
fun Method.findFreeRegister(
    startIndex: Int,
    vararg registersToExclude: Int
) = findFreeRegisters(startIndex, registersToExclude.toList()).first()

private fun Method.findFreeRegisters(
    startIndex: Int,
    registersToExclude: List<Int>
): List<Int> {
    // Build an array of instruction index to code offset.
    val offsetArray = buildInstructionOffsetArray()

    val freeRegisters = findFreeRegistersInternal(
        startIndex = startIndex,
        maxDepth = 2, // Follow branches up to 2 levels deep.
        currentDepth = 0,
        visitedIndices = mutableSetOf(),
        visitedBranches = mutableSetOf(),
        registersToExclude = registersToExclude.toSet(),
        offsetArray = offsetArray
    )

    if (freeRegisters.isEmpty()) {
        throw IllegalArgumentException("Could not find a free register from startIndex: " +
                "$startIndex excluding: $registersToExclude")
    }

    return freeRegisters.sorted()
}

private fun Method.buildInstructionOffsetArray(): IntArray {
    val instructionCount = instructions.count()
    val offsetArray = IntArray(instructionCount) { -1 }
    var currentOffset = 0

    for (i in 0 until instructionCount) {
        val instruction = getInstruction(i)
        val format = instruction.opcode.format

        if (!format.isPayloadFormat) {
            offsetArray[i] = currentOffset

            // Get size in bytes from format.
            val sizeInBytes = format.size
            val sizeInCodeUnits = when {
                sizeInBytes > 0 -> sizeInBytes / 2  // Normal instruction
                format == Format.UnresolvedOdexInstruction -> 1  // Default size
                else -> 1  // Fallback for any other edge case
            }

            currentOffset += sizeInCodeUnits
        }
        // Skip payloads
    }

    return offsetArray
}
/**
 * Gets all branch target indices for a branch instruction.
 * Returns empty list if not a branch or targets cannot be determined.
 *
 * @param instruction The branch instruction
 * @param currentIndex Current instruction index
 * @param offsetArray Array mapping instruction index to code offset (-1 for payloads)
 * @return List of target instruction indices
 */
private fun Method.getBranchTargets(
    instruction: Instruction,
    currentIndex: Int,
    offsetArray: IntArray
): List<Int> {
    val currentOffset = if (currentIndex < offsetArray.size) offsetArray[currentIndex] else -1
    if (currentOffset == -1) return emptyList() // This is a payload instruction.

    return when (instruction.opcode) {
        GOTO, GOTO_16, GOTO_32 -> {
            val offset = (instruction as OffsetInstruction).codeOffset
            val targetOffset = currentOffset + offset
            // Find the instruction index at this offset
            val targetIndex = findInstructionIndexByOffset(targetOffset, offsetArray)
            targetIndex?.let { listOf(it) } ?: emptyList()
        }
        IF_EQ, IF_NE, IF_LT, IF_GE, IF_GT, IF_LE,
        IF_EQZ, IF_NEZ, IF_LTZ, IF_GEZ, IF_GTZ, IF_LEZ -> {
            val offset = (instruction as OffsetInstruction).codeOffset
            val targetOffset = currentOffset + offset
            // Find the instruction index at this offset.
            val targetIndex = findInstructionIndexByOffset(targetOffset, offsetArray)
            targetIndex?.let { listOf(it) } ?: emptyList()
        }
        PACKED_SWITCH, SPARSE_SWITCH -> {
            // These need special handling - they jump to payloads
            // which then have their own target lists.
            emptyList() // Simplified for now
        }
        else -> emptyList()
    }
}

/**
 * Returns all free registers found starting from [startIndex].Follows branches up to [maxDepth].
 *
 * @param startIndex Inclusive starting index.
 * @param maxDepth Maximum branch nesting depth to follow (0 = don't follow branches).
 * @param currentDepth Current recursion depth.
 * @param visitedIndices Set of instruction indices already visited to avoid infinite loops.
 * @param visitedBranches Set of branch target indices already visited.
 * @param registersToExclude Registers to exclude from consideration.
 * @param registersToExclude Map from instruction index to code offset.
 * @return List of all free registers found.
 */
private fun Method.findFreeRegistersInternal(
    startIndex: Int,
    maxDepth: Int,
    currentDepth: Int,
    visitedIndices: MutableSet<Int>,
    visitedBranches: MutableSet<Int>,
    registersToExclude: Set<Int>,
    offsetArray: IntArray
): List<Int> {
    if (implementation == null) {
        throw IllegalArgumentException("Method has no implementation: $this")
    }
    if (startIndex < 0 || startIndex >= instructions.count()) {
        throw IllegalArgumentException("startIndex out of bounds: $startIndex")
    }

    // Avoid infinite recursion
    if (visitedIndices.contains(startIndex)) {
        return emptyList()
    }

    val usedRegisters = registersToExclude.toMutableSet()
    val freeRegisters = mutableSetOf<Int>()
    val branchTargets = mutableListOf<Int>()
    var encounteredBranch = false

    for (i in startIndex until instructions.count()) {
        // Mark this index as visited.
        visitedIndices.add(i)

        val instruction = getInstruction(i)
        val instructionRegisters = instruction.registersUsed

        // Check for write-only register.
        val writeRegister = instruction.writeRegister
        if (writeRegister != null && writeRegister !in usedRegisters) {
            // Check if this register is ONLY written to (not also read)
            // Count occurrences of writeRegister in instructionRegisters.
            val occurrences = instructionRegisters.count { it == writeRegister }
            // If it appears only once, it's write-only (the write).
            // If it appears more than once, it's also read.
            if (occurrences <= 1) {
                freeRegisters.add(writeRegister)
            }
        }

        // Mark all registers used by this instruction as "used".
        usedRegisters.addAll(instructionRegisters)

        // If we hit a return, all unused registers on this path are free.
        if (instruction.isReturnInstruction) {
            val allRegisters = (0 until implementation!!.registerCount).toSet()
            val unusedRegisters = allRegisters - usedRegisters
            freeRegisters.addAll(unusedRegisters)
            break
        }

        if (instruction.isBranchInstruction) {
            encounteredBranch = true

            // Get branch targets if we haven't exceeded max depth.
            if (currentDepth < maxDepth) {
                val targets = getBranchTargets(instruction, i, offsetArray)

                for (target in targets) {
                    if (target !in visitedBranches) {
                        branchTargets.add(target)
                        visitedBranches.add(target)
                    }
                }

                // For conditional branches, also consider the fall-through path.
                if (instruction.isConditionalBranch) {
                    val fallThrough = i + 1
                    if (fallThrough < instructions.count() && fallThrough !in visitedBranches) {
                        branchTargets.add(fallThrough)
                        visitedBranches.add(fallThrough)
                    }
                }
            }
        }
    }

    // If we encountered branches and we can follow them, collect free registers from all paths.
    if (encounteredBranch && currentDepth < maxDepth && branchTargets.isNotEmpty()) {
        val allFreeRegisters = mutableSetOf<Int>()
        allFreeRegisters.addAll(freeRegisters)

        for (targetIndex in branchTargets) {
            val targetFreeRegisters = findFreeRegistersInternal(
                startIndex = targetIndex,
                maxDepth = maxDepth,
                currentDepth = currentDepth + 1,
                visitedIndices = visitedIndices.toMutableSet(), // New set for each branch.
                visitedBranches = visitedBranches,
                registersToExclude = usedRegisters,
                offsetArray = offsetArray
            )
            allFreeRegisters.addAll(targetFreeRegisters)
        }

        return allFreeRegisters.toList()
    }

    return freeRegisters.toList()
}

/**
 * Finds the instruction index for a given code offset.
 *
 * @param targetOffset Target code offset in 16-bit units
 * @param offsetArray Array mapping instruction index to code offset (-1 for payloads)
 * @return Instruction index at the target offset, or null if not found
 */
private fun Method.findInstructionIndexByOffset(
    targetOffset: Int,
    offsetArray: IntArray
): Int? {
    // Simple linear search using indexOfFirst
    val index = offsetArray.indexOfFirst { it == targetOffset }
    if (index >= 0) {
        return index
    }

    // Should never happen.
    // Code has been tested on hundreds of random methods on all instruction indices,
    // but maybe some weird code exists that this has overlooked.
    Logger.getLogger(FreeRegisterProvider.javaClass.name).warning(
        "Could not find exact instruction offset for method: $this at offset: $targetOffset. " +
                "Please file a bug report in the Morphe patches repo"
    )
    return null
}

/**
 * Starting from and including the instruction at index [startIndex],
 * finds the next register that is written to and not read from. If a return instruction
 * is encountered, then the lowest unused register is returned.
 *
 * This method can return a non 4-bit register, and the calling code may need to temporarily
 * swap register contents if a 4-bit register is required.
 *
 * @param startIndex Inclusive starting index.
 * @param registersToExclude Registers to exclude, and consider as used. For most use cases,
 *                           all registers used in injected code should be specified.
 * @return The lowest register number (usually a 4-bit register) that is free at the given index.
 * @throws IllegalArgumentException If no free registers exist at the given index.
 * @see [FreeRegisterProvider]
 */
fun Method.findFreeRegister(
    startIndex: Int,
    registersToExclude: List<Int>
) = findFreeRegisters(startIndex, registersToExclude).first()


/**
 * @return The registers used by this instruction.
 */
val Instruction.registersUsed: List<Int>
    get() = when (this) {
        is FiveRegisterInstruction -> {
            when (registerCount) {
                0 -> listOf()
                1 -> listOf(registerC)
                2 -> listOf(registerC, registerD)
                3 -> listOf(registerC, registerD, registerE)
                4 -> listOf(registerC, registerD, registerE, registerF)
                else -> listOf(registerC, registerD, registerE, registerF, registerG)
            }
        }

        is ThreeRegisterInstruction -> listOf(registerA, registerB, registerC)
        is TwoRegisterInstruction -> listOf(registerA, registerB)
        is OneRegisterInstruction -> listOf(registerA)
        is RegisterRangeInstruction -> (startRegister until (startRegister + registerCount)).toList()
        else -> emptyList()
    }

/**
 * @return The register that is written to by this instruction,
 *         or NULL if this is not a write opcode.
 */
val Instruction.writeRegister: Int?
    get() {
        if (this.opcode !in writeOpcodes) {
            return null
        }
        if (this !is OneRegisterInstruction) {
            throw IllegalStateException("Not a write instruction: $this")
        }
        return registerA
    }

/**
 * This differs from [isBranchInstruction] in that it does not include unconditional goto.
 *
 * @return If this instruction is a conditional branch (multiple branch paths).
 *
 */
internal val Instruction.isConditionalBranch: Boolean
    get() = this.opcode in conditionalBranchOpcodes

/**
 * @return If this instruction is an unconditional or conditional branch opcode.
 */
internal val Instruction.isBranchInstruction: Boolean
    get() = this.opcode in branchOpcodes

/**
 * @return If this instruction returns or throws.
 */
internal val Instruction.isReturnInstruction: Boolean
    get() = this.opcode in returnOpcodes
