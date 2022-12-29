package app.revanced.patches.twitter.layout.hideviews.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.twitter.layout.hideviews.fingerprints.InlineActionTypesFingerprint
import app.revanced.patches.twitter.layout.hideviews.fingerprints.TweetStatsContainerConstructorFingerprint
import app.revanced.patches.twitter.layout.hideviews.fingerprints.TweetStatsContainerWrapperConstructorFingerprint
import app.revanced.patches.twitter.layout.hideviews.fingerprints.TweetStatsViewDelegateBinderFingerprint
import org.jf.dexlib2.Opcode

class HideViewsBytecodePatch : BytecodePatch(
    listOf(
        InlineActionTypesFingerprint,
        TweetStatsContainerWrapperConstructorFingerprint,
        TweetStatsContainerConstructorFingerprint,
        TweetStatsViewDelegateBinderFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        removeViewsFromTimeline(context)
        removeTweetStatViewInitializer(context)
        removeTweetStatViewWrapperInitializer(context)
        removeViewDelegateBinderSubscription()
        return PatchResultSuccess()
    }

    private fun removeViewsFromTimeline(context: BytecodeContext) {
        val addViewsToActionBarMethodFingerprint = object : MethodFingerprint(
            opcodes = listOf(
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IF_EQZ,
            )
        ) {}
        transformMethodAtPattern(
            context,
            InlineActionTypesFingerprint,
            addViewsToActionBarMethodFingerprint
        ) { patternScanResult, method ->
            method.removeInstruction(patternScanResult.endIndex - 1)
        }
    }

    private fun removeTweetStatViewInitializer(context: BytecodeContext) {
        val returnFingerprint = object : MethodFingerprint(
            opcodes = listOf(Opcode.RETURN_VOID)
        ) {}
        transformMethodAtPattern(
            context,
            TweetStatsContainerConstructorFingerprint,
            returnFingerprint
        ) { patternScanResult, method ->
            method.removeInstructions(patternScanResult.endIndex - 3, 2)
        }
    }

    private fun removeTweetStatViewWrapperInitializer(context: BytecodeContext) {
        val wrapperReturnFingerprint = object : MethodFingerprint(
            opcodes = listOf(
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.RETURN_VOID,
            )
        ) {}
        transformMethodAtPattern(
            context,
            TweetStatsContainerWrapperConstructorFingerprint,
            wrapperReturnFingerprint
        ) { patternScanResult, method ->
            method.removeInstructions(patternScanResult.startIndex - 4, 3)
        }
    }

    private fun removeViewDelegateBinderSubscription() {
        transformMethod(TweetStatsViewDelegateBinderFingerprint) { result, method ->
            method.removeInstructions(result.scanResult.patternScanResult!!.startIndex - 4, 9)
        }
    }

    private fun transformMethodAtPattern(
        context: BytecodeContext, methodFingerprint: MethodFingerprint,
        patternFingerprint: MethodFingerprint, transformer: TransformerAtPattern
    ) {
        transformMethod(methodFingerprint) { result, method ->
            val patternResult = patternFingerprint.also {
                it.resolve(context, method, result.classDef)
            }.result!!
            transformer(patternResult.scanResult.patternScanResult!!, method)
        }
    }

    private fun transformMethod(methodFingerprint: MethodFingerprint, transformer: Transformer) {
        val result = methodFingerprint.result!!
        val method = result.mutableMethod
        transformer(result, method)
    }
}

private typealias Transformer = (MethodFingerprintResult, MutableMethod) -> Unit

private typealias TransformerAtPattern = (MethodFingerprintResult.MethodFingerprintScanResult.PatternScanResult, MutableMethod) -> Unit