package app.revanced.patches.messenger.inbox.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal val createInboxSubTabsFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    opcodes(
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom { method, classDef ->
        method.name == "run" && classDef.fields.any any@{ field ->
            if (field.name != "__redex_internal_original_name") return@any false
            (field.initialValue as? StringEncodedValue)?.value == "InboxSubtabsItemSupplierImplementation\$onSubscribe\$1"
        }
    }
}
