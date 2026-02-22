package app.revanced.patches.all.misc.gms

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.patch.PatchException
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method

/**
 * Utility functions for the universal GmsCore patch.
 */
object GmsCoreUtils {
    
    /**
     * Find the main activity onCreate method using heuristics.
     * 
     * Looks for:
     * 1. Classes extending Activity
     * 2. Methods named "onCreate"
     * 3. Signature: onCreate(Landroid/os/Bundle;)V
     * 
     * @param classes Set of all classes in the app
     * @return The onCreate method, or null if not found
     */
    fun findMainActivityOnCreate(classes: Set<ClassDef>): Method? {
        for (classDef in classes) {
            // Check if class extends Activity
            if (!isActivityClass(classDef)) continue
            
            // Look for onCreate method
            for (method in classDef.methods) {
                if (method.name == "onCreate" && 
                    method.parameters.size == 1 &&
                    method.parameters[0].type == "Landroid/os/Bundle;" &&
                    method.returnType == "V") {
                    return method
                }
            }
        }
        return null
    }
    
    /**
     * Check if a class is an Activity or extends Activity.
     * 
     * @param classDef The class to check
     * @return True if the class is an Activity, false otherwise
     */
    private fun isActivityClass(classDef: ClassDef): Boolean {
        var currentClass: String? = classDef.superclass
        
        // Traverse up the inheritance chain
        while (currentClass != null && currentClass != "Ljava/lang/Object;") {
            if (currentClass == "Landroid/app/Activity;" ||
                currentClass.contains("Activity")) {
                return true
            }
            // In a real implementation, we'd need to resolve the superclass
            // For now, we'll use a simple heuristic
            break
        }
        
        return classDef.type.contains("Activity")
    }
    
    /**
     * Validate that required fingerprints were found.
     * 
     * @param fingerprints Map of fingerprint names to fingerprints
     * @throws PatchException if any required fingerprint is null
     */
    fun validateFingerprints(fingerprints: Map<String, Fingerprint?>) {
        val missing = fingerprints.filter { it.value == null }.keys
        if (missing.isNotEmpty()) {
            throw PatchException("Required fingerprints not found: ${missing.joinToString(", ")}")
        }
    }
    
    /**
     * Extract package name from a class descriptor.
     * 
     * Example: Lcom/google/android/youtube/MainActivity; -> com.google.android.youtube
     * 
     * @param classDescriptor The class descriptor
     * @return The package name
     */
    fun extractPackageName(classDescriptor: String): String {
        val cleaned = classDescriptor.removePrefix("L").removeSuffix(";")
        val lastSlash = cleaned.lastIndexOf('/')
        return if (lastSlash > 0) {
            cleaned.substring(0, lastSlash).replace('/', '.')
        } else {
            cleaned.replace('/', '.')
        }
    }
    
    /**
     * Check if a package name is a Google app.
     * 
     * @param packageName The package name to check
     * @return True if it's a Google app, false otherwise
     */
    fun isGoogleApp(packageName: String): Boolean {
        return packageName.startsWith("com.google.") ||
               packageName.startsWith("com.android.") && packageName.contains("google")
    }
}
