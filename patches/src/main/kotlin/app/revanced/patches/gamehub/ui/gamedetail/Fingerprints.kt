package app.revanced.patches.gamehub.ui.gamedetail

import app.revanced.patcher.fingerprint

/**
 * Matches GameDetailActivity.onCreate(Bundle).
 */
internal val gameDetailOnCreateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/ui/gamedetail/GameDetailActivity;" &&
            method.name == "onCreate"
    }
}
