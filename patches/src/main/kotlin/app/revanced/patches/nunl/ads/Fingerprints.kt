package app.revanced.patches.nunl.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val jwPlayerConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    custom { methodDef, classDef ->
        classDef.type == "Lcom/jwplayer/pub/api/configuration/PlayerConfig${'$'}Builder;" && methodDef.name == "advertisingConfig"
    }
}

internal val screenMapperFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Lnl/nu/android/bff/domain/models/screen/ScreenEntity;")
    parameters("Lnl/nu/performance/api/client/objects/Screen;")

    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_EQZ,
        Opcode.CHECK_CAST
    )

    custom { methodDef, classDef ->
        classDef.type == "Lnl/nu/android/bff/data/mappers/ScreenMapper;" && methodDef.name == "map"
    }
}

internal val nextPageRepositoryImplFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Lnl/nu/android/bff/domain/models/Page;")
    parameters("Lnl/nu/performance/api/client/PacResponse;", "Ljava/lang/String;")

    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_EQZ,
        Opcode.CHECK_CAST
    )

    custom { methodDef, classDef ->
        classDef.type == "Lnl/nu/android/bff/data/repositories/NextPageRepositoryImpl;" && methodDef.name == "mapToPage"
    }
}
