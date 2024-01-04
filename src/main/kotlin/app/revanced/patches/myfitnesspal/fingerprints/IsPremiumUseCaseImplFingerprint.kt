package app.revanced.patches.myfitnesspal.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object IsPremiumUseCaseImplFingerprint :
                MethodFingerprint(
                                accessFlags = 1,
                                customFingerprint = { methodDef, _ ->
                                        methodDef.definingClass.endsWith(
                                                        "myfitnesspal/libs/ads/usecases/IsPremiumUseCaseImpl;"
                                        ) && methodDef.name == "doWork"
                                }
                )
