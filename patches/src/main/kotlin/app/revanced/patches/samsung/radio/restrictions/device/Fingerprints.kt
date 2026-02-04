package app.revanced.patches.samsung.radio.restrictions.device

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

internal val BytecodePatchContext.checkDeviceMethod by gettingFirstMethodDeclaratively {
    returnType("Z")
    instructions(
        predicates = unorderedAllOf(
        method { MethodUtil.methodSignaturesMatch(getSalesCodeMethodReference, this) },
        method { MethodUtil.methodSignaturesMatch(getCountryIsoMethodReference, this) }
    ))
}

val getSalesCodeMethodReference = ImmutableMethodReference(
    "Landroid/os/SemSystemProperties;",
    "getSalesCode",
    emptyList(),
    "Ljava/lang/String;",
)

val getCountryIsoMethodReference = ImmutableMethodReference(
    "Landroid/os/SemSystemProperties;",
    "getCountryIso",
    emptyList(),
    "Ljava/lang/String;",
)

