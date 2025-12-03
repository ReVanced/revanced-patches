package app.revanced.patches.all.misc.connectivity.telephony.sim.spoof

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.util.Locale

@Suppress("unused")
val spoofSimProviderPatch = bytecodePatch(
    name = "Spoof SIM provider",
    description = "Spoofs information about the SIM card provider.",
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

    fun isMccMncValid(it: Int?): Boolean = it == null || (it >= 10000 && it <= 999999)

    val networkCountryIso by isoCountryPatchOption(
        "networkCountryIso",
        "Network ISO country code",
    )

    val networkOperator by intOption(
        key = "networkOperator",
        title = "MCC+MNC network operator code",
        description = "The 5 or 6 digits MCC+MNC (Mobile Country Code + Mobile Network Code) of the network operator.",
        validator = { isMccMncValid(it) }
    )

    val networkOperatorName by stringOption(
        key = "networkOperatorName",
        title = "Network operator name",
        description = "The full name of the network operator.",
    )

    val simCountryIso by isoCountryPatchOption(
        "simCountryIso",
        "SIM ISO country code",
    )

    val simOperator by intOption(
        key = "simOperator",
        title = "MCC+MNC SIM operator code",
        description = "The 5 or 6 digits MCC+MNC (Mobile Country Code + Mobile Network Code) of the SIM operator.",
        validator = { isMccMncValid(it) }
    )

    val simOperatorName by stringOption(
        key = "simOperatorName",
        title = "SIM operator name",
        description = "The full name of the SIM operator.",
    )

    dependsOn(
        transformInstructionsPatch(
            filterMap = { _, _, instruction, instructionIndex ->
                if (instruction !is ReferenceInstruction) return@transformInstructionsPatch null

                val reference = instruction.reference as? MethodReference ?: return@transformInstructionsPatch null

                val match = MethodCall.entries.firstOrNull { search ->
                    MethodUtil.methodSignaturesMatch(reference, search.reference)
                } ?: return@transformInstructionsPatch null

                val replacement = when (match) {
                    MethodCall.NetworkCountryIso -> networkCountryIso?.lowercase()
                    MethodCall.NetworkOperator -> networkOperator?.toString()
                    MethodCall.NetworkOperatorName -> networkOperatorName
                    MethodCall.SimCountryIso -> simCountryIso?.lowercase()
                    MethodCall.SimOperator -> simOperator?.toString()
                    MethodCall.SimOperatorName -> simOperatorName
                }
                replacement?.let { instructionIndex to it }
            },
            transform = ::transformMethodCall,
        ),
    )
}

private fun transformMethodCall(
    mutableMethod: MutableMethod,
    entry: Pair<Int, String>,
) {
    val (instructionIndex, methodCallValue) = entry

    // Get the register which would have contained the return value
    val register = mutableMethod.getInstruction<OneRegisterInstruction>(instructionIndex + 1).registerA

    // Replace the move-result instruction with our fake value
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
    NetworkOperator(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getNetworkOperator",
            emptyList(),
            "Ljava/lang/String;",
        ),
    ),
    NetworkOperatorName(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getNetworkOperatorName",
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
    SimOperator(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSimOperator",
            emptyList(),
            "Ljava/lang/String;",
        ),
    ),
    SimOperatorName(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSimOperatorName",
            emptyList(),
            "Ljava/lang/String;",
        ),
    ),
}
