package app.revanced.patches.nunl.ads

import app.revanced.patcher.*
import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode val BytecodePatchContext.jwPlayerConfigMethod by gettingFirstMutableMethodDeclaratively {
    name("advertisingConfig")
    definingClass($$"Lcom/jwplayer/pub/api/configuration/PlayerConfig$Builder;")
    accessFlags(AccessFlags.PUBLIC)
}

internal val BytecodePatchContext.screenMapperMethodMatch by composingFirstMethod {
    name("map")
    definingClass("Lnl/nu/android/bff/data/mappers/ScreenMapper;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Lnl/nu/android/bff/domain/models/screen/ScreenEntity;")
    parameterTypes("Lnl/nu/performance/api/client/objects/Screen;")
    instructions(
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.IF_EQZ(),
        Opcode.CHECK_CAST(),
    )
}

internal val BytecodePatchContext.nextPageRepositoryImplMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("Lnl/nu/android/bff/domain/models/Page;")
    parameterTypes("Lnl/nu/performance/api/client/PacResponse;", "Ljava/lang/String;")

    definingClass("Lnl/nu/android/bff/data/repositories/NextPageRepositoryImpl;")
    name("mapToPage")

    instructions(
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.IF_EQZ(),
        Opcode.CHECK_CAST(),
    )
}
