package app.revanced.patches.myfitnesspal.ads

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isPremiumUseCaseImplMethod by gettingFirstMutableMethodDeclaratively {
    name("doWork")
    definingClass("IsPremiumUseCaseImpl;"::endsWith)
    accessFlags(AccessFlags.PUBLIC)
}

internal val BytecodePatchContext.mainActivityNavigateToNativePremiumUpsellMethod by gettingFirstMutableMethodDeclaratively {
    name("navigateToNativePremiumUpsell")
    definingClass("MainActivity;"::endsWith)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
}
