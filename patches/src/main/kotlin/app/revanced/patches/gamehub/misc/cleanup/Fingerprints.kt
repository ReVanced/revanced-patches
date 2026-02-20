package app.revanced.patches.gamehub.misc.cleanup

import app.revanced.patcher.fingerprint

/**
 * Matches PromotionalDialogFragment.initView() — the private setup method
 * that displays promotional popup content. Returning early prevents the
 * popup from rendering.
 */
internal val promotionalDialogFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/view/popup/PromotionalDialogFragment;" &&
            method.name == "initView"
    }
}
