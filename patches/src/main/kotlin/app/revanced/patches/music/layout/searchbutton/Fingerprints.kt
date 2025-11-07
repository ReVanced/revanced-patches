package app.revanced.patches.music.layout.searchbutton

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val searchActionViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters()
    literal { searchButton }
    custom { _, classDef ->
        classDef.type.endsWith("/SearchActionProvider;")
    }
}
