package app.revanced.patches.microsoft.officelens.misc.onedrive

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val hasMigratedToOneDriveFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("FREManager;") && method.name == "getMigrationStage"
    }
}
