package app.revanced.patches.all.misc.spoof

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val spoofKeystoreSecurityLevelPatch = bytecodePatch(
    name = "Spoof keystore security level",
    description = "Forces apps to see Keymaster and Attestation security levels as 'StrongBox' (Level 2).",
    use = false
) {
    apply {
        classDefs.toList().forEach { classDef ->
            val mutableClass = classDefs.getOrReplaceMutable(classDef)

            mutableClass.methods.forEach { method ->
                val name = method.name.lowercase()

                // Match methods like getKeymasterSecurityLevel or getAttestationSecurityLevel
                if (name.contains("securitylevel") && method.returnType == "I") {
                    if (method.implementation?.instructions?.iterator()?.hasNext() == true) {
                        method.replaceInstructions(0, "const/4 v0, 0x2\nreturn v0")
                    }
                }
            }
        }
    }
}