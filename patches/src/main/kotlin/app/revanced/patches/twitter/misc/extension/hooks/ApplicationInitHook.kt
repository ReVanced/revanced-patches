package app.revanced.patches.twitter.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook =
    extensionHook {
        custom { method, classDef ->
           classDef.type == "Lcom/twitter/app/TwitterApplication;" && method.name == "onCreate"
        }
    }