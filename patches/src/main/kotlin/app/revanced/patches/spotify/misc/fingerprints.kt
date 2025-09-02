package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Primary fingerprints
internal val accountAttributeFingerprint = fingerprint(fuzzyPatternScanThreshold = 3) {
    returns("Ljava/util/Map;", "Ljava/lang/Object;") // Handle both return types
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.RETURN_OBJECT
    )
    strings("premium", "ads", "streaming") // Common attribute keys
    custom { method, classDef -> 
        (classDef.type.contains("spotify") || classDef.type.contains("config")) && 
        (classDef.type.contains("Account") || classDef.type.contains("Premium") || 
         classDef.type.contains("Config") || classDef.type.contains("State"))
    }
}

// Backup fingerprint for account attributes
internal val accountAttributeBackupFingerprint = fingerprint(fuzzyPatternScanThreshold = 4) {
    accessFlags(AccessFlags.PUBLIC)
    returns("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    )
    custom { _, classDef ->
        classDef.interfaces.any { it.contains("spotify") || it.contains("premium") }
    }
}

// Primary product state fingerprint
internal val productStateProtoFingerprint = fingerprint(fuzzyPatternScanThreshold = 3) {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/util/Map;", "Ljava/lang/Object;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL
    )
    strings("type", "premium", "client") // Common product state keys
    custom { method, classDef ->
        (classDef.type.contains("spotify") || classDef.type.contains("config")) &&
        (classDef.type.contains("Product") || classDef.type.contains("Premium") || 
         classDef.type.contains("State") || classDef.type.contains("Config"))
    }
}

// Backup product state fingerprint
internal val productStateBackupFingerprint = fingerprint(fuzzyPatternScanThreshold = 4) {
    accessFlags(AccessFlags.PUBLIC)
    returns("L")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST
    )
    custom { method, classDef ->
        method.parameterTypes.any { it.contains("Map") } &&
        (classDef.type.contains("spotify") || classDef.interfaces.any { it.contains("premium") })
    }
}

// Additional fingerprint for premium features
internal val premiumFeatureFingerprint = fingerprint(fuzzyPatternScanThreshold = 3) {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    parameters()
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL
    )
    custom { method, classDef ->
        (method.name.contains("premium", true) || method.name.contains("feature", true)) &&
        (classDef.type.contains("spotify") || classDef.type.contains("premium"))
    }
}
