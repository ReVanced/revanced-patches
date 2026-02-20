package app.revanced.patches.gamehub.misc.analytics

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.gamehub.misc.stability.appNullSafetyPatch
import app.revanced.util.asSequence
import app.revanced.util.returnEarly

/** Native libraries belonging to analytics/crash/tracking SDKs. */
private val ANALYTICS_NATIVE_LIBS = listOf(
    "libumeng-spy.so",
    "libcrashsdk.so",
    "libucrash-core.so",
    "libucrash.so",
    "libumonitor.so",
    "libalicomphonenumberauthsdk_core.so",
)

/** Substrings that identify analytics/tracking components in `android:name`. */
private val ANALYTICS_PATTERNS = listOf("firebase", "umeng", "analytics", "measurement")

/** Manifest permissions used exclusively for ad tracking. */
private val AD_TRACKING_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
)

private fun String.isAnalyticsComponent() =
    ANALYTICS_PATTERNS.any { lowercase().contains(it) }

private val analyticsCleanupResourcePatch = resourcePatch {
    execute {
        // Remove analytics/crash/tracking SDK native libraries.
        val libDir = get("lib")
        if (libDir.exists() && libDir.isDirectory) {
            libDir.listFiles()?.forEach { archDir ->
                if (archDir.isDirectory) {
                    ANALYTICS_NATIVE_LIBS.forEach { lib ->
                        if (archDir.resolve(lib).exists()) delete("lib/${archDir.name}/$lib")
                    }
                }
            }
        }

        // Remove Tencent Open SDK tracking config.
        delete("assets/com.tencent.open.config.json")

        // Remove analytics components, meta-data, and ad-tracking permissions from the manifest.
        document("AndroidManifest.xml").use { dom ->
            // Application-level components (providers, services, activities, receivers, meta-data).
            listOf("provider", "service", "activity", "receiver", "meta-data").flatMap { tag ->
                dom.getElementsByTagName(tag).asSequence().filter { node ->
                    node.attributes.getNamedItem("android:name")?.nodeValue
                        ?.isAnalyticsComponent() == true
                }.toList()
            }.forEach { node ->
                node.parentNode.removeChild(node)
            }

            // Ad-tracking permissions.
            dom.getElementsByTagName("uses-permission").asSequence().filter { node ->
                node.attributes.getNamedItem("android:name")?.nodeValue in AD_TRACKING_PERMISSIONS
            }.toList().forEach { node ->
                node.parentNode.removeChild(node)
            }
        }

        // Belt-and-suspenders: set the analytics-deactivated bool to true.
        document("res/values/bools.xml").use { dom ->
            dom.getElementsByTagName("bool").asSequence().filter { node ->
                node.attributes.getNamedItem("name")?.nodeValue == "FIREBASE_ANALYTICS_DEACTIVATED"
            }.forEach { node ->
                node.textContent = "true"
            }
        }
    }
}

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Disables Umeng, Firebase, and Jiguang analytics, removes tracking components " +
        "from the manifest, strips ad-tracking permissions, and deletes analytics/crash native libraries.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    dependsOn(analyticsCleanupResourcePatch, appNullSafetyPatch)

    execute {
        // Umeng
        umengAppFingerprint.method.returnEarly()
        umengAppModuleFingerprint.method.returnEarly()
        iUmengServiceImplAFingerprint.method.returnEarly()
        iUmengServiceImplBFingerprint.method.returnEarly()
        iUmengServiceImplCFingerprint.method.returnEarly()
        iUmengServiceImplDFingerprint.method.returnEarly()
        iUmengServiceImplEFingerprint.method.returnEarly()
        iUmengServiceImplFFingerprint.method.returnEarly()
        iUmengServiceImplOnEventFingerprint.method.returnEarly()

        // Firebase — prevent auto-init entirely.
        firebaseInitProviderFingerprint.method.returnEarly()

        // Jiguang — kill session analytics (PushSA).
        pushSAOnResumeFingerprint.method.returnEarly()
        pushSAOnPauseFingerprint.method.returnEarly()
        pushSAOnKillProcessFingerprint.method.returnEarly()
    }
}
