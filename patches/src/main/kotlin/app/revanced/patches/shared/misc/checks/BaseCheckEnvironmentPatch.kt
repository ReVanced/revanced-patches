package app.revanced.patches.shared.misc.checks

import android.os.Build.*
import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableEncodedValue
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableLongEncodedValue
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableStringEncodedValue
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import com.android.tools.smali.dexlib2.immutable.value.ImmutableLongEncodedValue
import com.android.tools.smali.dexlib2.immutable.value.ImmutableStringEncodedValue
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/checks/CheckEnvironmentPatch;"

fun checkEnvironmentPatch(
    mainActivityOnCreateFingerprint: Fingerprint,
    extensionPatch: Patch<*>,
    vararg compatiblePackages: String,
) = bytecodePatch(
    description = "Checks, if the application was patched by, otherwise warns the user.",
) {
    compatibleWith(*compatiblePackages)

    dependsOn(
        extensionPatch,
        addResourcesPatch,
    )

    execute {
        addResources("shared", "misc.checks.checkEnvironmentPatch")

        fun setPatchInfo() {
            fun <T : MutableEncodedValue> Fingerprint.setClassFields(vararg fieldNameValues: Pair<String, T>) {
                val fieldNameValueMap = mapOf(*fieldNameValues)

                classDef.fields.forEach { field ->
                    field.initialValue = fieldNameValueMap[field.name] ?: return@forEach
                }
            }

            patchInfoFingerprint.setClassFields(
                "PATCH_TIME" to System.currentTimeMillis().encoded,
            )

            fun setBuildInfo() {
                patchInfoBuildFingerprint.setClassFields(
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
                    "PATCH_PRODUCT" to PRODUCT.encodedAndHashed,
                    "PATCH_RADIO" to RADIO.encodedAndHashed,
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
            } catch (_: ClassNotFoundException) {
            }
        }

        fun invokeCheck() = mainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->check(Landroid/app/Activity;)V",
        )

        setPatchInfo()
        invokeCheck()
    }
}

@OptIn(ExperimentalEncodingApi::class)
private val String.encodedAndHashed
    get() = MutableStringEncodedValue(
        ImmutableStringEncodedValue(
            Base64.encode(
                MessageDigest.getInstance("SHA-1")
                    .digest(this.toByteArray(StandardCharsets.UTF_8)),
            ),
        ),
    )

private val Long.encoded get() = MutableLongEncodedValue(ImmutableLongEncodedValue(this))
