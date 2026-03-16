package app.revanced.patches.all.misc.spoof

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.forEachInstructionAsSequence

@Suppress("unused")
val spoofRootOfTrustPatch = bytecodePatch(
    name = "Spoof root of trust",
    description = "Spoofs device integrity states (Locked Bootloader, Verified OS) for apps that perform local certificate attestation.",
    use = false
) {
    apply {
        forEachInstructionAsSequence(
            match = { _, method, _, _ ->
                MethodCall.entries.firstOrNull {
                    method.definingClass.endsWith(it.className) &&
                            method.name == it.methodName &&
                            method.returnType == it.returnType
                }
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
    val className: String,
    val methodName: String,
    val returnType: String,
    val returnTrue: Boolean,
) {
    IsDeviceLockedRootOfTrust(
        "RootOfTrust;",
        "isDeviceLocked",
        "Z",
        "const/4 v0, 0x1\nreturn v0",
    ),
    GetVerifiedBootStateRootOfTrust(
        "RootOfTrust;",
        "getVerifiedBootState",
        "I",
        "const/4 v0, 0x0\nreturn v0",
    ),
    IsDeviceLockedAttestation(
        "Attestation;",
        "isDeviceLocked",
        "Z",
        "const/4 v0, 0x1\nreturn v0",
    ),
    GetVerifiedBootStateAttestation(
        "Attestation;",
        "getVerifiedBootState",
        "I",
        "const/4 v0, 0x0\nreturn v0",
    ),
}