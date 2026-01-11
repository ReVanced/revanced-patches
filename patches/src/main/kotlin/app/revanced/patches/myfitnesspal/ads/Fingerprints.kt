package app.revanced.patches.myfitnesspal.ads

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isPremiumUseCaseImplMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    definingClass("IsPremiumUseCaseImpl;")
    name("doWork")
}

internal val BytecodePatchContext.mainActivityNavigateToNativePremiumUpsellMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    definingClass("MainActivity;")
    name("navigateToNativePremiumUpsell")
}
