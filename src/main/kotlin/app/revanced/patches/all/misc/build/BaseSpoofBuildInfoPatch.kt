package app.revanced.patches.all.misc.build

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

abstract class BaseSpoofBuildInfoPatch : BaseTransformInstructionsPatch<Pair<Int, Pair<String, String>>>() {
    // The build information supported32BitAbis, supported64BitAbis, and supportedAbis are not supported for now,
    // because initializing an array in transform is a bit more complex.

    protected open val board: String? = null

    protected open val bootloader: String? = null

    protected open val brand: String? = null

    protected open val cpuAbi: String? = null

    protected open val cpuAbi2: String? = null

    protected open val device: String? = null

    protected open val display: String? = null

    protected open val fingerprint: String? = null

    protected open val hardware: String? = null

    protected open val host: String? = null

    protected open val id: String? = null

    protected open val manufacturer: String? = null

    protected open val model: String? = null

    protected open val odmSku: String? = null

    protected open val product: String? = null

    protected open val radio: String? = null

    protected open val serial: String? = null

    protected open val sku: String? = null

    protected open val socManufacturer: String? = null

    protected open val socModel: String? = null

    protected open val tags: String? = null

    protected open val time: Long? = null

    protected open val type: String? = null

    protected open val user: String? = null


    // Lazy, so that patch options above are initialized before they are accessed.
    private val replacements: Map<String, Pair<String, String>> by lazy {
        buildMap {
            if (board != null) put("BOARD", "const-string" to "\"$board\"")
            if (bootloader != null) put("BOOTLOADER", "const-string" to "\"$bootloader\"")
            if (brand != null) put("BRAND", "const-string" to "\"$brand\"")
            if (cpuAbi != null) put("CPU_ABI", "const-string" to "\"$cpuAbi\"")
            if (cpuAbi2 != null) put("CPU_ABI2", "const-string" to "\"$cpuAbi2\"")
            if (device != null) put("DEVICE", "const-string" to "\"$device\"")
            if (display != null) put("DISPLAY", "const-string" to "\"$display\"")
            if (fingerprint != null) put("FINGERPRINT", "const-string" to "\"$fingerprint\"")
            if (hardware != null) put("HARDWARE", "const-string" to "\"$hardware\"")
            if (host != null) put("HOST", "const-string" to "\"$host\"")
            if (id != null) put("ID", "const-string" to "\"$id\"")
            if (manufacturer != null) put("MANUFACTURER", "const-string" to "\"$manufacturer\"")
            if (model != null) put("MODEL", "const-string" to "\"$model\"")
            if (odmSku != null) put("ODM_SKU", "const-string" to "\"$odmSku\"")
            if (product != null) put("PRODUCT", "const-string" to "\"$product\"")
            if (radio != null) put("RADIO", "const-string" to "\"$radio\"")
            if (serial != null) put("SERIAL", "const-string" to "\"$serial\"")
            if (sku != null) put("SKU", "const-string" to "\"$sku\"")
            if (socManufacturer != null) put("SOC_MANUFACTURER", "const-string" to "\"$socManufacturer\"")
            if (socModel != null) put("SOC_MODEL", "const-string" to "\"$socModel\"")
            if (tags != null) put("TAGS", "const-string" to "\"$tags\"")
            if (time != null) put("TIME", "const-wide" to "$time")
            if (type != null) put("TYPE", "const-string" to "\"$type\"")
            if (user != null) put("USER", "const-string" to "\"$user\"")
        }
    }

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ): Pair<Int, Pair<String, String>>? {
        val reference = instruction.getReference<FieldReference>() ?: return null
        if (reference.definingClass != BUILD_CLASS_DESCRIPTOR) return null

        return replacements[reference.name]?.let { instructionIndex to it }
    }

    override fun transform(mutableMethod: MutableMethod, entry: Pair<Int, Pair<String, String>>) {
        val (index, replacement) = entry
        val (opcode, operand) = replacement
        val register = mutableMethod.getInstruction<OneRegisterInstruction>(index).registerA

        mutableMethod.replaceInstruction(index, "$opcode v$register, $operand")
    }

    private companion object {
        private const val BUILD_CLASS_DESCRIPTOR = "Landroid/os/Build;"
    }
}