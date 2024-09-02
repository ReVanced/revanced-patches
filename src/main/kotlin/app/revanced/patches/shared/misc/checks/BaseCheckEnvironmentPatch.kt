package app.revanced.patches.shared.misc.checks

import android.os.Build.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableEncodedValue
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableLongEncodedValue
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableStringEncodedValue
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.checks.fingerprints.PatchInfoBuildFingerprint
import app.revanced.patches.shared.misc.checks.fingerprints.PatchInfoFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch
import app.revanced.util.exception
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.immutable.value.ImmutableLongEncodedValue
import com.android.tools.smali.dexlib2.immutable.value.ImmutableStringEncodedValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.logging.Logger
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

abstract class BaseCheckEnvironmentPatch(
    private val mainActivityOnCreateFingerprint: MethodFingerprint,
    compatiblePackages: Set<CompatiblePackage>,
    integrationsPatch: BaseIntegrationsPatch,
) : BytecodePatch(
    name = "Check environment",
    description = "Checks, if the application was patched by the user, otherwise warns the user.",
    compatiblePackages = compatiblePackages,
    dependencies = setOf(
        AddResourcesPatch::class,
        integrationsPatch::class,
    ),
    fingerprints = setOf(
        PatchInfoFingerprint,
        PatchInfoBuildFingerprint,
        mainActivityOnCreateFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(BaseCheckEnvironmentPatch::class)

        setPatchInfo()
        invokeCheck()
    }

    private fun setPatchInfo() {
        PatchInfoFingerprint.setClassFields(
            // Use last three digits to prevent brute forcing the hashed IP.
            "PUBLIC_IP_DURING_PATCH" to (publicIp?.takeLast(3) ?: "").encodedAndHashed,
            "PATCH_TIME" to System.currentTimeMillis().encoded,
        )

        fun setBuildInfo() {
            PatchInfoBuildFingerprint.setClassFields(
                "PATCH_BOARD" to BOARD.encodedAndHashed,
                "PATCH_BOOTLOADER" to BOOTLOADER.encodedAndHashed,
                "PATCH_BRAND" to BRAND.encodedAndHashed,
                "PATCH_CPU_ABI" to CPU_ABI.encodedAndHashed,
                "PATCH_CPU_ABI2" to CPU_ABI2.encodedAndHashed,
                "PATCH_DEVICE" to DEVICE.encodedAndHashed,
                "PATCH_DISPLAY" to DISPLAY.encodedAndHashed,
                "PATCH_FINGERPRINT" to FINGERPRINT.encodedAndHashed,
                "PATCH_HARDWARE" to HARDWARE.encodedAndHashed,
                "PATCH_HOST" to HOST.encodedAndHashed,
                "PATCH_ID" to ID.encodedAndHashed,
                "PATCH_MANUFACTURER" to MANUFACTURER.encodedAndHashed,
                "PATCH_MODEL" to MODEL.encodedAndHashed,
                "PATCH_ODM_SKU" to ODM_SKU.encodedAndHashed,
                "PATCH_PRODUCT" to PRODUCT.encodedAndHashed,
                "PATCH_RADIO" to RADIO.encodedAndHashed,
                "PATCH_SERIAL" to SERIAL.encodedAndHashed,
                "PATCH_SKU" to SKU.encodedAndHashed,
                "PATCH_SOC_MANUFACTURER" to SOC_MANUFACTURER.encodedAndHashed,
                "PATCH_SOC_MODEL" to SOC_MODEL.encodedAndHashed,
                "PATCH_TAGS" to TAGS.encodedAndHashed,
                "PATCH_TYPE" to TYPE.encodedAndHashed,
                "PATCH_USER" to USER.encodedAndHashed,
            )
        }

        try {
            Class.forName("android.os.Build")
            // This only works on Android,
            // because it uses Android APIs.
            setBuildInfo()
        } catch (_: ClassNotFoundException) { }
    }

    private fun invokeCheck() = mainActivityOnCreateFingerprint.result?.mutableMethod?.addInstructions(
        0,
        "invoke-static/range { p0 .. p0 },$INTEGRATIONS_CLASS_DESCRIPTOR->check(Landroid/app/Activity;)V",
    ) ?: throw mainActivityOnCreateFingerprint.exception

    private companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/shared/checks/CheckEnvironmentPatch;"

        @OptIn(ExperimentalEncodingApi::class)
        private val String.encodedAndHashed
            get() = MutableStringEncodedValue(
                ImmutableStringEncodedValue(
                    Base64.encode(MessageDigest.getInstance("SHA-1").digest(toByteArray())),
                ),
            )

        private val Long.encoded get() = MutableLongEncodedValue(ImmutableLongEncodedValue(this))

        private fun <T : MutableEncodedValue> MethodFingerprint.setClassFields(vararg fieldNameValues: Pair<String, T>) {
            val fieldNameValueMap = mapOf(*fieldNameValues)

            resultOrThrow().mutableClass.fields.forEach { field ->
                field.initialValue = fieldNameValueMap[field.name] ?: return@forEach
            }
        }

        private val publicIp: String?
            get() {
                // Using multiple services to increase reliability, distribute the load and minimize tracking.
                val getIpServices = listOf(
                    "https://wtfismyip.com/text",
                    "https://whatsmyfuckingip.com/text",
                    "https://api.ipify.org?format=text",
                    "https://icanhazip.com",
                    "https://ifconfig.me/ip",
                )

                var publicIP: String? = null

                try {
                    val service = getIpServices[Random.Default.nextInt(getIpServices.size - 1)]
                    val urlConnection = URL(service).openConnection() as HttpURLConnection

                    try {
                        val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))

                        publicIP = reader.readLine()

                        reader.close()
                    } finally {
                        urlConnection.disconnect()
                    }
                } catch (e: Exception) {
                    // If the app does not have the INTERNET permission or the service is down,
                    // the public IP can not be retrieved.
                    Logger.getLogger(this::class.simpleName).severe("Failed to get public IP address: " + e.message)
                }

                return publicIP
            }
    }
}
