package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.fingerprint

internal val addSettingsEntryFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/SettingNewVersionFragment;") &&
            method.name == "initUnitManger"
    }
}

internal val adPersonalizationActivityOnCreateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/AdPersonalizationActivity;") &&
            method.name == "onCreate"
    }
}

internal val settingsEntryFingerprint = fingerprint {
    strings("pls pass item or extends the EventUnit")
}

internal val settingsEntryInfoFingerprint = fingerprint {
    strings(
        "ExposeItem(title=",
        ", icon=",
    )
}

internal val settingsStatusLoadFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("Lapp/revanced/extension/tiktok/settings/SettingsStatus;") &&
            method.name == "load"
    }
}

internal val supportGroupDefaultStateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/SupportGroupVM;") && method.name == "defaultState"
    }
}

internal val openDebugCellVmDefaultStateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/OpenDebugCellVM;") && method.name == "defaultState"
    }
}

internal val openDebugCellStateConstructorFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("LX/05iN;") &&
            method.name == "<init>" &&
            method.parameterTypes == listOf(
                "LX/05hd;",
                "Ljava/lang/Integer;",
                "Ljava/lang/Integer;",
                "Ljava/lang/Integer;",
                "Lkotlin/jvm/internal/AwS526S0100000_2;",
            )
    }
}

internal val openDebugCellComposeFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "LIZ" &&
            method.returnType == "V" &&
            method.parameterTypes == listOf(
                "LX/05iN;",
                "Z",
                "Z",
                "LX/06c6;",
                "I",
            )
    }
}

internal val openDebugCellClickWrapperFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("Lkotlin/jvm/internal/AwS350S0200000_2;") &&
            method.name == "invoke\$85" &&
            method.parameterTypes == listOf("Lkotlin/jvm/internal/AwS350S0200000_2;")
    }
}
