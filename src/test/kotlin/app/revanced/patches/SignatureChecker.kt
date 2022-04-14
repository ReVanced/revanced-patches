package app.revanced.patches

import app.revanced.patcher.Patcher
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.PatternScanMethod
import org.jf.dexlib2.iface.Method
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
            if (!signature.resolved) {
                unresolved.add(signature)
                continue
            }

            val patternScanMethod = signature.metadata.patternScanMethod
            if (patternScanMethod is PatternScanMethod.Fuzzy) {
                val warnings = patternScanMethod.warnings!!
                println("Signature ${signature.metadata.name} had ${warnings.size} warnings!")
                val method = signature.result!!.method
                val instructions = method.implementation!!.instructions
                println("class = ${method.definingClass}, method = ${printMethod(method)}")
                for (warning in warnings) {
                    println("-".repeat(10))
                    for (i in (warning.actualIndex - 5).coerceAtLeast(0) until warning.actualIndex) {
                        println("$i: ${instructions[i].opcode}")
                    }
                    println("${warning.actualIndex}: $warning")
                    for (i in warning.actualIndex + 1 until (warning.actualIndex + 5).coerceAtMost(instructions.size)) {
                        println("$i: ${instructions[i].opcode}")
                    }
                }
                println("=".repeat(20))
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

    private fun printMethod(method: Method): String {
        return "${method.name}(${method.parameterTypes.joinToString("")})${method.returnType}"
    }
}