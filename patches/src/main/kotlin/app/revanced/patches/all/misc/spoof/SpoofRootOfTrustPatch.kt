package app.revanced.patches.all.misc.spoof

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

@Suppress("unused")
val spoofRootOfTrustPatch = bytecodePatch(
    name = "Spoof root of trust",
    description = "Spoofs device integrity states (Locked Bootloader, Verified OS) for apps that perform local certificate attestation.",
    use = false
) {
    apply {
        forEachInstructionAsSequence(
            match = { _, method, _, _ ->
                MethodCall.entries.firstOrNull { MethodUtil.methodSignaturesMatch(method, it.reference) }
            },
            transform = { mutableMethod, methodCall ->
                if (mutableMethod.implementation?.instructions?.iterator()?.hasNext() == true) {
                    mutableMethod.replaceInstructions(0, methodCall.replacementInstructions)
                }
            }
        )
    }
}

private enum class MethodCall(
    val reference: MethodReference,
    val replacementInstructions: String,
) {
    IsDeviceLockedRootOfTrust(
        ImmutableMethodReference(
            "LRootOfTrust;",
            "isDeviceLocked",
            emptyList(),
            "Z"
        ),
        "const/4 v0, 0x1\nreturn v0",
    ),
    GetVerifiedBootStateRootOfTrust(
        ImmutableMethodReference(
            "LRootOfTrust;",
            "getVerifiedBootState",
            emptyList(),
            "I"
        ),
        "const/4 v0, 0x0\nreturn v0",
    ),
    IsDeviceLockedAttestation(
        ImmutableMethodReference(
            "LAttestation;",
            "isDeviceLocked",
            emptyList(),
            "Z"
        ),
        "const/4 v0, 0x1\nreturn v0",
    ),
    GetVerifiedBootStateAttestation(
        ImmutableMethodReference(
            "LAttestation;",
            "getVerifiedBootState",
            emptyList(),
            "I"
        ),
        "const/4 v0, 0x0\nreturn v0",
    ),
}