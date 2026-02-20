package app.revanced.patches.gamehub.network

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val drakeNetInterceptorFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/drake/net/interceptor/NetOkHttpInterceptor;" &&
            method.implementation?.instructions?.any { instruction ->
                (instruction as? ReferenceInstruction)?.reference?.let {
                    it is MethodReference && it.name == "newBuilder" &&
                        it.returnType == "Lokhttp3/Request\$Builder;"
                } == true
            } == true
    }
}

internal val wifiuiNetInterceptorFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/adb/wifiui/net/interceptor/NetOkHttpInterceptor;" &&
            method.implementation?.instructions?.any { instruction ->
                (instruction as? ReferenceInstruction)?.reference?.let {
                    it is MethodReference && it.name == "newBuilder" &&
                        it.returnType == "Lokhttp3/Request\$Builder;"
                } == true
            } == true
    }
}

internal val eggGameHttpConfigFingerprint = fingerprint {
    strings("https://landscape-api.vgabc.com/")
    custom { _, classDef ->
        classDef.type == "Lcom/xj/common/http/EggGameHttpConfig;"
    }
}

internal val wifiuiHttpConfigFingerprint = fingerprint {
    strings("https://landscape-api.vgabc.com/")
    custom { method, classDef ->
        classDef.type == "Lcom/xj/adb/wifiui/http/HttpConfig;" && method.name == "b"
    }
}

// GsonConverter — return null from catch block instead of throwing ConvertException,
// so JSON parse failures from the alternative API are treated as missing data, not crashes.
internal val gsonConverterFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/common/http/GsonConverter;" &&
            method.implementation?.instructions?.any { instr ->
                instr.getReference<TypeReference>()?.type ==
                    "Lcom/drake/net/exception/ConvertException;"
            } == true
    }
}
