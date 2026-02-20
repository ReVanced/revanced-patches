package app.revanced.patches.gamehub.steam.storage

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val appMetadataSetInstallPathFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/standalone/steam/data/bean/AppMetadata;" &&
            method.name == "setInstallPath"
    }
}

internal val steamDownloadExtendSetInstallDirPathFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/standalone/steam/data/bean/SteamDownloadExtend;" &&
            method.name == "setInstallDirPath"
    }
}

internal val steamDownloadInfoHelperFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/standalone/steam/core/SteamDownloadInfoHelper;" &&
            method.name == "a" &&
            method.implementation?.instructions?.any { instruction ->
                (instruction as? ReferenceInstruction)?.reference?.let {
                    it is MethodReference && it.name == "setInstallPath"
                } == true
            } == true
    }
}

// I: DownloadGameSizeInfoDialog$computeAvailableSize$2 — lambda that computes available storage
// for the download dialog. Override to return the correct value for the active storage location.
internal val downloadDialogStorageFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/ui/dialog/" +
            "DownloadGameSizeInfoDialog${'$'}computeAvailableSize${'$'}2;" &&
            method.implementation?.instructions?.any { instr ->
                instr.getReference<MethodReference>()?.name == "getExternalStorageDirectory"
            } == true
    }
}
