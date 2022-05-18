package app.revanced.patches

import app.revanced.patcher.Patcher
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patcher.signature.implementation.method.annotation.PatternScanMethod
import org.jf.dexlib2.iface.Method
import org.junit.Test
import java.io.File

internal class SignatureChecker {
    @Test
    fun checkMethodSignatures() {

        // FIXME: instead of having this as a test, it should be turned into a task which can be ran manually
        val file = File("stock.apk")
        if (!file.exists()) {
            throw IllegalStateException("Missing $file! To run this test, please place stock.apk here: ${file.absolutePath}")
        }
        val patcher = Patcher(file, "signatureCheckerCache", false)
        patcher.addPatches(Index.patches.map { it() })
        val unresolved = mutableListOf<String>()
        for (signature in patcher.resolveSignatures()) {
            val signatureAnnotations = signature::class.annotations

            val nameAnnotation = signatureAnnotations.find { it is Name } as Name
            if (!signature.resolved) {
                unresolved.add(nameAnnotation.name)
                continue
            }

            val patternScanMethod =
                signatureAnnotations.find { it::class.annotations.any { method -> method is PatternScanMethod } }
            if (patternScanMethod is FuzzyPatternScanMethod) {
                val warnings = signature.result!!.scanData.warnings!!
                val method = signature.result!!.method

                val methodFromMetadata =
                    signatureAnnotations.find { it is MatchingMethod } as MatchingMethod? ?: MatchingMethod()

                println("Signature: ${nameAnnotation}.\nMethod: ${methodFromMetadata.definingClass}->${methodFromMetadata.name} (Signature matches: ${method.definingClass}->${method.toStr()})\nWarnings: ${warnings.count()}")
                for (warning in warnings) {
                    println("${warning.instructionIndex} / ${warning.patternIndex}: ${warning.wrongOpcode} (expected: ${warning.correctOpcode})")
                }

                println("=".repeat(20))
            }
        }
        if (unresolved.isNotEmpty()) {
            val base = Exception("${unresolved.size} signatures were not resolved.")
            for (name in unresolved) {
                base.addSuppressed(Exception("Signature $name was not resolved!"))
            }
            throw base
        }
    }

    private fun Method.toStr(): String {
        return "${this.name}(${this.parameterTypes.joinToString("")})${this.returnType}"
    }
}