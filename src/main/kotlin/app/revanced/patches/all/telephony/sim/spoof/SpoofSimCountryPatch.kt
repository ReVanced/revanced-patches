package app.revanced.patches.all.telephony.sim.spoof

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.util.patch.AbstractTransformInstructionsPatch
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.util.*


@Patch(
    name = "Spoof SIM country",
    description = "Spoofs country information returned by the SIM card provider.",
    use = false,
)
@Suppress("unused")
object SpoofSimCountryPatch : AbstractTransformInstructionsPatch<Pair<Int, String>>() {
    private val isoValidator: PatchOption<String>.(String?) -> Boolean =
        { it: String? -> it?.uppercase() in Locale.getISOCountries() || it == null }

    private val networkCountryIso by stringPatchOption(
        "networkCountryIso",
        null,
        null,
        "Network ISO Country Code",
        "ISO-3166-1 alpha-2 country code equivalent of the MCC (Mobile Country Code) " +
                "of the current registered operator or the cell nearby.",
        validator = isoValidator
    )

    private val simCountryIso by stringPatchOption(
        "simCountryIso",
        null,
        null,
        "Sim ISO Country Code",
        "ISO-3166-1 alpha-2 country code equivalent for the SIM provider's country code.",
        validator = isoValidator
    )

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ): Pair<Int, String>? {
        if (instruction !is ReferenceInstruction) return null

        val reference = instruction.reference as? MethodReference ?: return null

        val match = MethodCall.entries.firstOrNull { search ->
            MethodUtil.methodSignaturesMatch(reference, search.reference)
        } ?: return null

        val iso = when (match) {
            MethodCall.NetworkCountryIso -> networkCountryIso
            MethodCall.SimCountryIso -> simCountryIso
        }?.lowercase()

        return iso?.let { instructionIndex to it }
    }

    override fun transform(
        mutableMethod: MutableMethod,
        entry: Pair<Int, String>
    ) = transformMethodCall(entry, mutableMethod)

    private fun transformMethodCall(
        entry: Pair<Int, String>,
        mutableMethod: MutableMethod
    ) {
        val (instructionIndex, methodCallValue) = entry

        val register = mutableMethod.getInstruction<OneRegisterInstruction>(instructionIndex + 1).registerA

        mutableMethod.replaceInstruction(
            instructionIndex + 1,
            "const-string v$register, \"$methodCallValue\""
        )
    }

    private enum class MethodCall(
        val reference: MethodReference
    ) {
        NetworkCountryIso(
            ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getNetworkCountryIso",
            emptyList(),
            "Ljava/lang/String;"
            )
        ),
        SimCountryIso(
            ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSimCountryIso",
            emptyList(),
            "Ljava/lang/String;"
            )
        )
    }
}
