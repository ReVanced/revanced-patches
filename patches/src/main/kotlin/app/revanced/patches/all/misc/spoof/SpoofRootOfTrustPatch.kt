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
                // Filter by class reference first to avoid unnecessary processing.
                if (!(method.definingClass.contains("RootOfTrust") || method.definingClass.contains("Attestation"))) return@forEachInstructionAsSequence null

                when (method.name) {
                    "isDeviceLocked" -> if (method.returnType == "Z") method else null
                    "getVerifiedBootState" -> if (method.returnType == "I") method else null
                    else -> null
                }
            },
            transform = { mutableMethod, _ ->
                if (mutableMethod.implementation?.instructions?.iterator()?.hasNext() == true) {
                    when (mutableMethod.name) {
                        "isDeviceLocked" -> mutableMethod.replaceInstructions(0, "const/4 v0, 0x1\nreturn v0")
                        "getVerifiedBootState" -> mutableMethod.replaceInstructions(0, "const/4 v0, 0x0\nreturn v0")
                    }
                }
            }
        )
    }
}