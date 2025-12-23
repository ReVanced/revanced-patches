package app.revanced.patches.strava.mediaupload

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal const val CLASS_NAME_SUFFIX = "/MediaUploadParameters;"

internal val getCompressionQualityFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT)
    custom { method, classDef ->
        classDef.endsWith(CLASS_NAME_SUFFIX) && method.name == "getCompressionQuality"
    }
}

internal val getMaxDurationFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT)
    custom { method, classDef ->
        classDef.endsWith(CLASS_NAME_SUFFIX) && method.name == "getMaxDuration"
    }
}

internal val getMaxSizeFingerprint = fingerprint {
    opcodes(Opcode.IGET)
    custom { method, classDef ->
        classDef.endsWith(CLASS_NAME_SUFFIX) && method.name == "getMaxSize"
    }
}
