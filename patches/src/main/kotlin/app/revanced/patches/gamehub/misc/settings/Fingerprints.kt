package app.revanced.patches.gamehub.misc.settings

import app.revanced.patcher.fingerprint

private const val ENTITY_CLASS = "Lcom/xj/landscape/launcher/data/model/entity/SettingItemEntity;"
private const val VIEW_MODEL_CLASS = "Lcom/xj/landscape/launcher/vm/SettingItemViewModel;"
private const val HOLDER_CLASS = "Lcom/xj/landscape/launcher/ui/setting/holder/SettingSwitchHolder;"

internal val settingItemEntityFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == ENTITY_CLASS && method.name == "<clinit>"
    }
}

internal val settingItemViewModelFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == VIEW_MODEL_CLASS && method.name == "l"
    }
}

internal val settingSwitchHolderFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == HOLDER_CLASS && method.name == "w"
    }
}

internal val settingItemEntityTitleFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == ENTITY_CLASS && method.name == "getContentName"
    }
}

internal val settingSwitchHolderBindFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == HOLDER_CLASS && method.name == "u"
    }
}
