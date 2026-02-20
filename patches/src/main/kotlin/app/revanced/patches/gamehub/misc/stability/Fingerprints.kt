package app.revanced.patches.gamehub.misc.stability

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// F: App — Koin module iteration method that calls Collection.add(module).
// Matched by class + exactly one Collection.add() invocation (narrows to the module list builder).
internal val appKoinModuleIterationFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/app/App;" &&
            method.implementation?.instructions?.count { instr ->
                instr.getReference<MethodReference>()?.name == "add"
            } == 1
    }
}
