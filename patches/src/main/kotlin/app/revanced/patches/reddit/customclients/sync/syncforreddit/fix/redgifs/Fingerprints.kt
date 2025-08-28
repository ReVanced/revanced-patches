package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.redgifs

import app.revanced.patcher.fingerprint


internal val createOkHttpClientFingerprint = fingerprint {
    custom { methodDef, classDef ->
        classDef.sourceFile == "OkHttpHelper.java" && methodDef.name == "k"
    }
}