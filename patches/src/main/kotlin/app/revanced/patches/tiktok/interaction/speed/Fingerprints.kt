package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getSpeedMethod by gettingFirstMutableMethodDeclaratively {
    name("onFeedSpeedSelectedEvent")
    definingClass { endsWith("/BaseListFragmentPanel;") }
}

internal val BytecodePatchContext.setSpeedMethod by gettingFirstMutableMethodDeclaratively("enterFrom") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Ljava/lang/String;", "Lcom/ss/android/ugc/aweme/feed/model/Aweme;", "F")
}
