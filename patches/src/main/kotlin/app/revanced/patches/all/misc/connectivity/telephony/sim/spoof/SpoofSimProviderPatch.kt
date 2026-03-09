package app.revanced.patches.all.misc.connectivity.telephony.sim.spoof

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.stringOption
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.util.*

@Suppress("unused")
val spoofSIMProviderPatch = bytecodePatch(
    name = "Spoof SIM provider",
    description = "Spoofs information about the SIM card provider.",
    use = false,
) {
    val countries = Locale.getISOCountries().associateBy { Locale("", it).displayCountry }

    fun isoCountryPatchOption(
        name: String,
    ) = stringOption(
        name,
        null,
        countries,
        "ISO-3166-1 alpha-2 country code equivalent for the SIM provider's country code.",
        false,
        validator = { it: String? -> it == null || it.uppercase() in countries.values },
    )

    fun isMccMncValid(it: Int?): Boolean = it == null || (it >= 10000 && it <= 999999)
    fun isNumericValid(it: String?, length: Int): Boolean = it.isNullOrBlank() || it.equals("random", ignoreCase = true) || it.length == length

    val networkCountryIso by isoCountryPatchOption("Network ISO country code")

    val networkOperator by intOption(
        name = "MCC+MNC network operator code",
        description = "The 5 or 6 digits MCC+MNC (Mobile Country Code + Mobile Network Code) of the network operator.",
        validator = { isMccMncValid(it) },
    )

    val networkOperatorName by stringOption(
        name = "Network operator name",
        description = "The full name of the network operator.",
    )

    val simCountryIso by isoCountryPatchOption("SIM ISO country code")

    val simOperator by intOption(
        name = "MCC+MNC SIM operator code",
        description = "The 5 or 6 digits MCC+MNC (Mobile Country Code + Mobile Network Code) of the SIM operator.",
        validator = { isMccMncValid(it) },
    )

    val simOperatorName by stringOption(
        name = "SIM operator name",
        description = "The full name of the SIM operator.",
    )

    val imei by stringOption(
        name = "IMEI value",
        description = "Enter a 15-digit IMEI to spoof, leave blank to skip, or type 'random' to generate a valid random IMEI.",
        validator = { isNumericValid(it, 15) },
    )

    val meid by stringOption(
        name = "MEID value",
        description = "Enter a 14-character hex MEID to spoof, leave blank to skip, or type 'random' to generate a valid random MEID.",
        validator = { isNumericValid(it, 14) },
    )

    val imsi by stringOption(
        name = "IMSI (Subscriber ID)",
        description = "15-digit IMSI to spoof, blank to skip, or 'random'.",
        validator = { isNumericValid(it, 15) },
    )

    val iccid by stringOption(
        name = "ICCID (SIM Serial)",
        description = "19-digit ICCID to spoof, blank to skip, or 'random'.",
        validator = { isNumericValid(it, 19) },
    )

    val phone by stringOption(
        name = "Phone Number",
        description = "Phone number to spoof, blank to skip, or 'random'.",
        validator = { it.isNullOrBlank() || it.equals("random", ignoreCase = true) || it.startsWith("+") },
    )

    dependsOn(
        bytecodePatch {
            apply {
                fun generateRandomNumeric(length: Int) = (1..length).map { ('0'..'9').random() }.joinToString("")

                val computedImei = if (imei.isNullOrBlank()) {
                    null
                } else if (imei?.equals("random", ignoreCase = true) == true) {
                    val prefix = "86" + (1..12).map { ('0'..'9').random() }.joinToString("")
                    var sum = 0
                    for (i in prefix.indices) {
                        var d = prefix[i] - '0'
                        if (i % 2 != 0) {
                            d *= 2
                            if (d > 9) d -= 9
                        }
                        sum += d
                    }
                    val checkDigit = (10 - (sum % 10)) % 10
                    prefix + checkDigit
                } else {
                    imei
                }

                val computedMeid = if (meid.isNullOrBlank()) {
                    null
                } else if (meid?.equals("random", ignoreCase = true) == true) {
                    val chars = "0123456789ABCDEF"
                    (1..14).map { chars.random() }.joinToString("")
                } else {
                    meid?.uppercase()
                }

                val computedImsi = if (imsi.isNullOrBlank()) {
                    null
                } else if (imsi?.equals("random", ignoreCase = true) == true) {
                    generateRandomNumeric(15)
                } else {
                    imsi
                }

                val computedIccid = if (iccid.isNullOrBlank()) {
                    null
                } else if (iccid?.equals("random", ignoreCase = true) == true) {
                    "89" + generateRandomNumeric(17)
                } else {
                    iccid
                }

                val computedPhone = if (phone.isNullOrBlank()) {
                    null
                } else if (phone?.equals("random", ignoreCase = true) == true) {
                    "+" + generateRandomNumeric(11)
                } else {
                    phone
                }

                forEachInstructionAsSequence(
                    match = { _, _, instruction, instructionIndex ->
                        if (instruction !is ReferenceInstruction) return@forEachInstructionAsSequence null

                        val reference = instruction.reference as? MethodReference ?: return@forEachInstructionAsSequence null

                        val match = MethodCall.entries.firstOrNull { search ->
                            MethodUtil.methodSignaturesMatch(reference, search.reference)
                        } ?: return@forEachInstructionAsSequence null

                        val replacement = when (match) {
                            MethodCall.NetworkCountryIso -> networkCountryIso?.lowercase()
                            MethodCall.NetworkOperator -> networkOperator?.toString()
                            MethodCall.NetworkOperatorName -> networkOperatorName
                            MethodCall.SimCountryIso -> simCountryIso?.lowercase()
                            MethodCall.SimOperator -> simOperator?.toString()
                            MethodCall.SimOperatorName -> simOperatorName
                            MethodCall.Imei,
                            MethodCall.ImeiWithSlot,
                            MethodCall.DeviceId,
                            MethodCall.DeviceIdWithSlot -> computedImei
                            MethodCall.Meid,
                            MethodCall.MeidWithSlot -> computedMeid
                            MethodCall.SubscriberId,
                            MethodCall.SubscriberIdWithSlot -> computedImsi
                            MethodCall.SimSerialNumber,
                            MethodCall.SimSerialNumberWithSlot -> computedIccid
                            MethodCall.Line1Number,
                            MethodCall.Line1NumberWithSlot -> computedPhone
                        }
                        replacement?.let { instructionIndex to it }
                    },
                    transform = ::transformMethodCall
                )
            }
        },
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
    Imei(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getImei",
            emptyList(),
            "Ljava/lang/String;"
        ),
    ),
    ImeiWithSlot(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getImei",
            listOf("I"),
            "Ljava/lang/String;"
        ),
    ),
    DeviceId(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getDeviceId",
            emptyList(),
            "Ljava/lang/String;"
        ),
    ),
    DeviceIdWithSlot(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getDeviceId",
            listOf("I"),
            "Ljava/lang/String;"
        ),
    ),
    Meid(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getMeid",
            emptyList(),
            "Ljava/lang/String;"
        ),
    ),
    MeidWithSlot(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getMeid",
            listOf("I"),
            "Ljava/lang/String;"
        ),
    ),
    SubscriberId(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSubscriberId",
            emptyList(),
            "Ljava/lang/String;"
        )
    ),
    SubscriberIdWithSlot(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSubscriberId",
            listOf("I"),
            "Ljava/lang/String;"
        )
    ),
    SimSerialNumber(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSimSerialNumber",
            emptyList(),
            "Ljava/lang/String;"
        )
    ),
    SimSerialNumberWithSlot(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getSimSerialNumber",
            listOf("I"),
            "Ljava/lang/String;"
        )
    ),
    Line1Number(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getLine1Number",
            emptyList(),
            "Ljava/lang/String;"
        )
    ),
    Line1NumberWithSlot(
        ImmutableMethodReference(
            "Landroid/telephony/TelephonyManager;",
            "getLine1Number",
            listOf("I"),
            "Ljava/lang/String;"
        )
    )
}
