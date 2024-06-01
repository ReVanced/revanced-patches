package app.revanced.patches.messenger.inbox.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal object CreateInboxSubTabsFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    ),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "run" && classDef.fields.any any@{ field ->
            if (field.name != "__redex_internal_original_name") return@any false
            (field.initialValue as? StringEncodedValue)?.value == "InboxSubtabsItemSupplierImplementation\$onSubscribe\$1"
        }
    },
)
