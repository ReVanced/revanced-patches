package app.revanced.patches.nunl.ads

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.jwPlayerConfigMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    definingClass($$"Lcom/jwplayer/pub/api/configuration/PlayerConfig$Builder;")
    name("advertisingConfig")
}

internal val screenMapperMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Lnl/nu/android/bff/domain/models/screen/ScreenEntity;")
    parameterTypes("Lnl/nu/performance/api/client/objects/Screen;")

    definingClass("Lnl/nu/android/bff/data/mappers/ScreenMapper;")
    name("map")

    instructions(
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.IF_EQZ(),
        Opcode.CHECK_CAST(),
    )
}

internal val nextPageRepositoryImplMethodMatch = firstMethodComposite {
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
