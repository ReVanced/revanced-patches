# 💉 Introduction to [ReVanced Patcher](https://github.com/revanced/revanced-patcher)

Familiarize yourself with [ReVanced Patcher](https://github.com/revanced/revanced-patcher).

## 📙 How it works

```kt
/**
 * Load ReVanced Patches and ReVanced Integrations.
 *
 * PatchBundleLoader can be used to load a list of patches from the supplied file in a new ClassLoader instance.
 * You can set their options and supply them to ReVanced Patcher.
 *
 * Executing patches multiple times from the same ClassLoader instance may fail because they may not reset their state.
 * Therefore, a new PatchBundleLoader should be used for every execution of ReVanced Patcher.
 */
val patches = PatchBundleLoader.Jar(File("revanced-patches.jar"))
val integrations = listOf(File("integrations.apk"))

/**
 * Instantiate ReVanced Patcher with options.
 * This will decode the app manifest of the input file to read package metadata
 * such as package name and version code.
 */
val options = PatcherOptions(inputFile = File("some.apk"))
Patcher(options).use { patcher ->
    val patcherResult = patcher.apply {
        acceptIntegrations(integrations)
        acceptPatches(patches)

        // Execute patches.
        runBlocking {
            patcher.apply(false).collect { patchResult ->
                if (patchResult.exception != null)
                    println("${patchResult.patchName} failed:\n${patchResult.exception}")
                else
                    println("${patchResult.patchName} succeeded")
            }
        }
    }.get()

    // Compile patched DEX files and resources.
    val result = patcher.get()

    val dexFiles = result.dexFiles // Patched DEX files.
    val resourceFile = result.resourceFile // File containing patched resources.
    val doNotCompress = result.doNotCompress // Files that should not be compressed.
}
```

## ⏭️ Whats next

The next section will give you an understanding of a patch.

Continue: [🧩 Skeleton of a Patch](2_skeleton.md)
