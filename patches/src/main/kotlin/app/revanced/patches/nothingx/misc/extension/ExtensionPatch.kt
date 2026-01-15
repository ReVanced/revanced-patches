package app.revanced.patches.nothingx.misc.extension

import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.extension.extensionHook

val extensionPatch = sharedExtensionPatch(
    extensionName = "nothingx",
    extensionHook {
        custom { method, classDef ->
            method.name == "onCreate" && classDef.contains("BaseApplication")
        }
    },
)