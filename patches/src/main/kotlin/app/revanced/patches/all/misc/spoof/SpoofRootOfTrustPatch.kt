package app.revanced.patches.all.misc.spoof

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val spoofRootOfTrustPatch = bytecodePatch(
    name = "Spoof root of trust",
    description = "Spoofs device integrity states (Locked Bootloader, Verified OS) for apps that perform local certificate attestation.",
    use = false
) {
    apply {
        classDefs.toList().filter { it.type.contains("RootOfTrust") || it.type.contains("Attestation") }.forEach { classDef ->
            val mutableClass = classDefs.getOrReplaceMutable(classDef)

            mutableClass.methods.forEach { method ->
                when (method.name) {
                    "isDeviceLocked" -> {
                        if (method.returnType == "Z" && method.implementation?.instructions?.iterator()?.hasNext() == true) {
                            method.replaceInstructions(0, "const/4 v0, 0x1\nreturn v0")
                        }
                    }
                    "getVerifiedBootState" -> {
                        if (method.returnType == "I" && method.implementation?.instructions?.iterator()?.hasNext() == true) {
                            method.replaceInstructions(0, "const/4 v0, 0x0\nreturn v0") // KM_VERIFIED_BOOT_VERIFIED
                        }
                    }
                }
            }
        }
    }
}