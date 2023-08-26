package app.revanced.patches.messenger.inputfield.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.dexbacked.value.DexBackedStringEncodedValue

object SendTypingIndicatorFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf(),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "run" && classDef.fields.any {
            it.name == "__redex_internal_original_name"
                    && (it.initialValue as? DexBackedStringEncodedValue)?.value == "ConversationTypingContext\$sendActiveStateRunnable\$1"
        }
    }
)
