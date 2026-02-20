package app.revanced.patches.gamehub.ui.statusbar

import app.revanced.patcher.fingerprint

/**
 * Matches BatteryUtil.a(Context, ImageView)V — the method that reads
 * battery level via BatteryManager.getIntProperty(4) and sets the icon.
 */
internal val batteryUtilFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/common/utils/BatteryUtil;" &&
            method.name == "a" &&
            method.parameterTypes.size == 2 &&
            method.parameterTypes[0] == "Landroid/content/Context;" &&
            method.parameterTypes[1] == "Landroid/widget/ImageView;"
    }
}
