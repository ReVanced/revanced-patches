package app.revanced.patches.youtube.misc.fix.contentprovider

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.methodCall
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.unstableContentProviderMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/content/ContentResolver;", "[Ljava/lang/String;")
    instructions(
        // Early targets use HashMap and later targets use ConcurrentMap.
        methodCall(
            name = "putAll",
            parameters = listOf("Ljava/util/Map;"),
        ),
        addString("ContentProvider query returned null cursor"),
    )
}
