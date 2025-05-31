package app.revanced.patches.all.misc.connectivity.telephony.sim.spoof

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.util.*

@Suppress("unused")
val spoofSimCountryPatch = bytecodePatch(
    name = "Spoof SIM country",
    description = "Spoofs country information returned by the SIM card provider.",
    use = false,
) {
    val countries = Locale.getISOCountries().associateBy { Locale("", it).displayCountry }

    fun isoCountryPatchOption(
        key: String,
        title: String,
    ) = stringOption(
        key,
        null,
        countries,
        title,
        "ISO-3166-1 alpha-2 country code equivalent for the SIM provider's country code.",
        false,
        validator = { it: String? -> it == null || it.uppercase() in countries.values },
    )

    val networkCountryIso by isoCountryPatchOption(
        "networkCountryIso",
        "Network ISO country code",
    )

    val simCountryIso by isoCountryPatchOption(
        "simCountryIso",
        "SIM ISO country code",
    )

    dependsOn(
        transformInstructionsPatch(
            filterMap = { _, _, instruction, instructionIndex ->
                if (instruction !is ReferenceInstruction) return@transformInstructionsPatch null

                val reference = instruction.reference as? MethodReference ?: return@transformInstructionsPatch null

                val match = MethodCall.entries.firstOrNull { search ->
                    MethodUtil.methodSignaturesMatch(reference, search.reference)
                } ?: return@transformInstructionsPatch null

                val iso = when (match) {
                    MethodCall.NetworkCountryIso -> networkCountryIso
                    MethodCall.SimCountryIso -> simCountryIso
                }?.lowercase()

                iso?.let { instructionIndex to it }
            },
            transform = { mutableMethod, entry: Pair<Int, String> ->
                transformMethodCall(entry, mutableMethod)
            },
        ),
    )
}

private fun transformMethodCall(
    entry: Pair<Int, String>,
    mutableMethod: MutableMethod,
) {
    val (instructionIndex, methodCallValue) = entry

    val register = mutableMethod.getInstruction<OneRegisterInstruction>(instructionIndex + 1).registerA

    mutableMethod.replaceInstruction(
        instructionIndex + 1,
        "const-string v$register, \"$methodCallValue\"",
    )
}

private enum class MethodCall(
    val reference: MethodReference,
) {
    NetworkCountryIso(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getNetworkCountryIso",
            emptyList(),
            "Ljava/lang/String;",
        ),
    ),
    SimCountryIso(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSimCountryIso",
            emptyList(),
            "Ljava/lang/String;",
        ),
    ),
}
