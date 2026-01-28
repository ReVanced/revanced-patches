package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.httpClientBuilderMethod by gettingFirstMethod("client == null", "scheduler == null")
