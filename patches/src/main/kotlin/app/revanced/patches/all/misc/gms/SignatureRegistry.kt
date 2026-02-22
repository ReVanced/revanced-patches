package app.revanced.patches.all.misc.gms

/**
 * Registry of known app package signatures for GmsCore spoofing.
 * 
 * Each app needs its original signature to be spoofed so that GmsCore
 * recognizes it as the legitimate app for authentication purposes.
 */
internal object SignatureRegistry {
    private val signatures = mapOf(
        // YouTube
        "com.google.android.youtube" to "24bb24c05e47e0aefa68a58a766179d9b613a600",
        
        // YouTube Music
        "com.google.android.apps.youtube.music" to "afb0fed5eeaebdd86f56a97742f4b6b33ef59875",
        
        // Google Photos
        "com.google.android.apps.photos" to "24bb24c05e47e0aefa68a58a766179d9b613a600",
        
        // Google News (Magazines)
        "com.google.android.apps.magazines" to "24bb24c05e47e0aefa68a58a766179d9b613a666",
    )
    
    /**
     * Get the signature for a given package name.
     * 
     * @param packageName The original package name of the app
     * @return The signature string, or null if not found
     */
    fun getSignature(packageName: String): String? = signatures[packageName]
    
    /**
     * Check if a signature is registered for the given package.
     * 
     * @param packageName The original package name of the app
     * @return True if a signature is registered, false otherwise
     */
    fun hasSignature(packageName: String): Boolean = signatures.containsKey(packageName)
    
    /**
     * Get all registered package names.
     * 
     * @return Set of all registered package names
     */
    fun getRegisteredPackages(): Set<String> = signatures.keys
}
