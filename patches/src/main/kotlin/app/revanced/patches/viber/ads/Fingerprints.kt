package app.revanced.patches.viber

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.parameters
import com.android.tools.smali.dexlib2.AccessFlags


internal val adsFreeFingerprint = fingerprint {
    returns("I")
    parameters()
    custom { method, classDef ->
        classDef.type.contains("com/viber/voip/feature/viberplus") &&
        (classDef.accessFlags and AccessFlags.FINAL) != 0 &&
        classDef.superclass?.contains("com/viber/voip/core/feature") == true &&  // Must extend com.viber.voip.core.feature.?
        classDef.methods.count() == 1 &&
        (method.accessFlags and AccessFlags.PUBLIC) != 0 &&
        (method.accessFlags and AccessFlags.FINAL) != 0
    }
}
