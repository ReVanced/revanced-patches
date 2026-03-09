package app.revanced.patches.all.misc.spoof

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.forEachInstructionAsSequence

@Suppress("unused")
val spoofKeystoreSecurityLevelPatch = bytecodePatch(
    name = "Spoof keystore security level",
    description = "Forces apps to see Keymaster and Attestation security levels as 'StrongBox' (Level 2).",
    use = false
) {
    apply {
        forEachInstructionAsSequence(
            match = { _, method, _, _ ->
                val name = method.name.lowercase()

                // Match methods like getKeymasterSecurityLevel or getAttestationSecurityLevel
                if (name.contains("securitylevel") && method.returnType == "I") {
                    method
                } else null
            },
            transform = { mutableMethod, _ ->
                // Ensure the method has an implementation before replacing
                if (mutableMethod.implementation?.instructions?.iterator()?.hasNext() == true) {
                    mutableMethod.replaceInstructions(0, "const/4 v0, 0x2\nreturn v0")
                }
            }
        )
    }
}