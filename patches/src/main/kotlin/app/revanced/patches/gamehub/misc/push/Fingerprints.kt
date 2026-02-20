package app.revanced.patches.gamehub.misc.push

import app.revanced.patcher.fingerprint

internal val pushAppFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/push/PushApp;" && method.name == "b"
    }
}

internal val pushAppModuleFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/push/PushApp;" && method.name == "a"
    }
}

// PermissionUtils.J — the synthetic default-argument wrapper that shows a pre-permission
// rationale dialog before calling requestPermissions. Gutting it suppresses the
// "Turn on message notifications" popup that appears on first launch.
internal val permissionDialogFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/common/utils/PermissionUtils;" && method.name == "J"
    }
}

// PermissionUtils.G — opens android.settings.APP_NOTIFICATION_SETTINGS.
// Called by SettingJumpHolder when the user taps "Push Notification" and notifications
// are disabled. Returning early prevents the jump to system settings.
internal val openNotificationSettingsFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/common/utils/PermissionUtils;" && method.name == "G"
    }
}

// UserNotificationSettingEntity synthetic default-arg constructor.
// The default value for all three notification toggles (game_recs, act_notify, news_pushes)
// is OPEN (1). When the server is unreachable, the error fallback creates a default entity,
// making all toggles appear enabled. We change the default to CLOSE (2).
internal val notificationSettingDefaultsFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/data/model/entity/UserNotificationSettingEntity;" &&
            method.name == "<init>" &&
            method.parameterTypes.size == 5
    }
}
