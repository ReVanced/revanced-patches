package app.revanced.patches

import app.revanced.patcher.Patcher
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.PatternScanMethod
import org.junit.Test
import java.io.File

internal class SignatureChecker {
    @Test
    fun checkSignatures() {
        val file = File("stock.apk")
        if (!file.exists()) {
            throw IllegalStateException("Missing stock.apk! To run this test, please place stock.apk here: ${file.absolutePath}")
        }
        val patcher = Patcher(file)
        patcher.addPatches(Index.patches.map { it() })
        val unresolved = mutableListOf<MethodSignature>()
        for (signature in patcher.resolveSignatures()) {
            if (!signature.resolved) unresolved.add(signature)

            val patternScanMethod = signature.metadata.patternScanMethod
            if (patternScanMethod is PatternScanMethod.Fuzzy) {
                val warnings = patternScanMethod.warnings
                println("Signature ${signature.metadata.name} had ${warnings.size} warnings!")
                for (warning in warnings) {
                    println(warning.toString())
                }
            }
        }
        if (unresolved.isNotEmpty()) {
            val base = Exception("${unresolved.size} signatures were not resolved.")
            for (signature in unresolved) {
                base.addSuppressed(Exception("Signature ${signature.metadata.name} was not resolved!"))
            }
            throw base
        }
    }
}