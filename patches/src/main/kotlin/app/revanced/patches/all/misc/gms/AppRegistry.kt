package app.revanced.patches.all.misc.gms

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.patch.Patch

/**
 * Configuration for an app's GmsCore support.
 * 
 * @param fromPackageName The original package name of the app
 * @param toPackageName The target ReVanced package name
 * @param signature The app's signature for spoofing
 * @param mainActivityFingerprint Fingerprint for the main activity onCreate method
 * @param primeMethodFingerprint Fingerprint for the "prime" method (optional)
 * @param earlyReturnFingerprints Set of fingerprints for methods that need early returns
 * @param additionalDependencies Additional patches this app depends on
 * @param compatibleVersions List of compatible app versions (optional)
 */
data class AppConfig(
    val fromPackageName: String,
    val toPackageName: String,
    val signature: String,
    val mainActivityFingerprint: Fingerprint,
    val primeMethodFingerprint: Fingerprint? = null,
    val earlyReturnFingerprints: Set<Fingerprint> = emptySet(),
    val additionalDependencies: Set<Patch<*>> = emptySet(),
    val compatibleVersions: List<String> = emptyList(),
)

/**
 * Registry for app-specific GmsCore configurations.
 * 
 * Apps can register their specific requirements here, allowing the universal
 * patch to handle app-specific edge cases while maintaining a common implementation.
 */
object AppRegistry {
    private val configs = mutableMapOf<String, AppConfig>()
    
    /**
     * Register an app configuration.
     * 
     * @param config The app configuration to register
     */
    fun register(config: AppConfig) {
        configs[config.fromPackageName] = config
    }
    
    /**
     * Get the configuration for a package.
     * 
     * @param packageName The original package name
     * @return The app configuration, or null if not registered
     */
    fun get(packageName: String): AppConfig? = configs[packageName]
    
    /**
     * Check if an app is registered.
     * 
     * @param packageName The original package name
     * @return True if the app is registered, false otherwise
     */
    fun isRegistered(packageName: String): Boolean = configs.containsKey(packageName)
    
    /**
     * Get all registered package names.
     * 
     * @return Set of all registered package names
     */
    fun getRegisteredPackages(): Set<String> = configs.keys
    
    /**
     * Generate a default ReVanced package name from an original package name.
     * 
     * Examples:
     * - com.google.android.youtube -> app.revanced.android.youtube
     * - com.google.android.apps.youtube.music -> app.revanced.android.apps.youtube.music
     * 
     * @param originalPackage The original package name
     * @return The generated ReVanced package name
     */
    fun generateReVancedPackageName(originalPackage: String): String {
        return if (originalPackage.startsWith("com.google.")) {
            originalPackage.replace("com.google.", "app.revanced.")
        } else {
            "app.revanced.$originalPackage"
        }
    }
}
