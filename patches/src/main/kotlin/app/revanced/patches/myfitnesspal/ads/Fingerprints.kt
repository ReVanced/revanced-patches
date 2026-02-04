package app.revanced.patches.myfitnesspal.ads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isPremiumUseCaseImplMethod by gettingFirstMethodDeclaratively {
    name("doWork")
    definingClass { endsWith("IsPremiumUseCaseImpl;") }
    accessFlags(AccessFlags.PUBLIC)
}

internal val BytecodePatchContext.mainActivityNavigateToNativePremiumUpsellMethod by gettingFirstMethodDeclaratively {
    name("navigateToNativePremiumUpsell")
    definingClass { endsWith("MainActivity;") }
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
}
