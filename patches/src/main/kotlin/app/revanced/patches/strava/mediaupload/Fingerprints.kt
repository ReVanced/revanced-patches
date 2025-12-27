package app.revanced.patches.strava.mediaupload

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val getCompressionQualityFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT)
    custom { method, _ ->
        method.name == "getCompressionQuality"
    }
}

internal val getMaxDurationFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT)
    custom { method, _ ->
        method.name == "getMaxDuration"
    }
}

internal val getMaxSizeFingerprint = fingerprint {
    opcodes(Opcode.IGET)
    custom { method, _ ->
        method.name == "getMaxSize"
    }
}
