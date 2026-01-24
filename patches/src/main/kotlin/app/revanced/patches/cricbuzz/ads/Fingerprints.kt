package app.revanced.patches.cricbuzz.ads

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.userStateSwitchMethod by gettingFirstMutableMethodDeclaratively("key.user.state", "NA") {
    opcodes(Opcode.SPARSE_SWITCH)
}

internal val BytecodePatchContext.cb11ConstructorMethod by gettingFirstMutableMethodDeclaratively {
    definingClass { endsWith("CB11Details;") }
    parameterTypes(
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
    )
}

internal val BytecodePatchContext.getBottomBarMethod by gettingFirstMutableMethodDeclaratively {
    name("getBottombar")
    definingClass { endsWith("HomeMenu;") }
}
