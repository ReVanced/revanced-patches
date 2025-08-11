package app.revanced.patches.orfon.detection.root

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val isDeviceRootedFingeprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    custom { method, classDef ->
        method.name == "isDeviceRooted" &&
            classDef.endsWith("/RootChecker;")
    }
}
