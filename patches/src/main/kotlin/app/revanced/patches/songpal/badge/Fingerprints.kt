package app.revanced.patches.songpal.badge

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

// Located @ ub.i0.h#p (9.5.0)
internal val BytecodePatchContext.createTabsMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE)
    returnType("Ljava/util/List;")
    instructions(
        method {
            parameterTypes.isEmpty() &&
                definingClass == ACTIVITY_TAB_DESCRIPTOR &&
                returnType == "[${ACTIVITY_TAB_DESCRIPTOR}"
        },
    )
}

// Located @ com.sony.songpal.mdr.vim.activity.MdrRemoteBaseActivity.e#run (9.5.0)
internal val BytecodePatchContext.showNotificationMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    returnType("V")
    instructions(method { MethodUtil.methodSignaturesMatch(this, expectedReference) })
}

internal val expectedReference = ImmutableMethodReference(
    "Lcom/google/android/material/bottomnavigation/BottomNavigationView;",
    "getOrCreateBadge", // Non-obfuscated placeholder method name.
    listOf("I"),
    "Lcom/google/android/material/badge/BadgeDrawable;",
)
