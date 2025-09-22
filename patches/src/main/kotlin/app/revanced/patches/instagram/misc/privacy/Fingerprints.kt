package app.revanced.patches.instagram.misc.privacy

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val testFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "A00" &&
                classDef.type == "LX/Hk9;"
    }
}
