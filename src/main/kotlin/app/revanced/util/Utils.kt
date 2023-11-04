package app.revanced.util

import app.revanced.extensions.exception
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint

object Utils {
    /**
     * Return the resolved methods of [MethodFingerprint]s early.
     */
    fun List<MethodFingerprint>.returnEarly(bool: Boolean = false) {
        val const = if (bool) "0x1" else "0x0"
        this.forEach { fingerprint ->
            fingerprint.result?.let { result ->
                val stringInstructions = when (result.method.returnType.first()) {
                    'L' -> """
                        const/4 v0, $const
                        return-object v0
                        """
                    'V' -> "return-void"
                    'I', 'Z' -> """
                        const/4 v0, $const
                        return v0
                        """
                    else -> throw Exception("This case should never happen.")
                }

                result.mutableMethod.addInstructions(0, stringInstructions)
            } ?: throw fingerprint.exception
        }
    }
}