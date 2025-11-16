package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val mediaJsonParserFingerprint = fingerprint {
    custom { method, classDef ->classDef.type == "LX/5rs;" && method.name == "A01"}
}

internal val mediaJsonParserFingerprint2 = fingerprint {
    custom { method, classDef ->classDef.type == "LX/6ht;" && method.name == "A2S"}
}

internal val mediaJsonParserFingerprint3 = fingerprint {
    custom { method, classDef ->classDef.type == "LX/7Sz;" && method.name == "A04"}
}

internal val mediaJsonParserFingerprint4 = fingerprint {
    custom { method, classDef ->classDef.type == "LX/5rs;" && method.name == "A09"}
}
