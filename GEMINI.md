# ReVanced Patches Project Context

This project contains a collection of ReVanced Patches for various Android applications (e.g., YouTube, Reddit, Instagram, etc.). Patches are implemented as Kotlin modules that use the ReVanced Patcher DSL to perform bytecode and resource transformations on target APKs.

## Project Structure

- `patches/`: The core module containing the patch implementations.
  - `src/main/kotlin/app/revanced/patches/<app>/`: Patch logic organized by application and category (e.g., `ad`, `layout`, `misc`).
  - `src/main/resources/`: Resources used by patches, such as `addresources` for XML resources (strings, layouts) and assets.
  - `strings-processing.gradle.kts`: Custom Gradle script for managing localized strings via Crowdin.
- `extensions/`: Android library sub-projects that provide helper code or new UI components to be injected into the patched applications.
  - `extensions/shared/library/`: Common library code used by multiple extensions.
  - `extensions/<app>/`: App-specific extension code.
- `gradle/libs.versions.toml`: Centralized dependency management using Version Catalogs.
- `package.json`: Contains configuration for semantic-release and project automation.

## Building and Running

The project uses Gradle for building and publishing.

- **Build all patches and extensions:**
  ```bash
  ./gradlew build
  ```
- **Publish patches to a repository:**
  ```bash
  ./gradlew publish
  ```
- **Clean the project:**
  ```bash
  ./gradlew clean
  ```
- **Process strings for Crowdin (flattening structure):**
  ```bash
  ./gradlew :patches:processStringsForCrowdin
  ```
- **Process strings from Crowdin (stripping prefixes):**
  ```bash
  ./gradlew :patches:processStringsFromCrowdin
  ```

## Development Conventions

- **Patch Implementation:** Patches are defined using `bytecodePatch` or `resourcePatch` builders. They include metadata like `name`, `description`, `dependencies`, and `compatibleWith` (versions of the target app).
- **Fingerprinting:** Bytecode patches rely on fingerprints (located in `Fingerprints.kt` within each patch package) to identify specific methods or fields in the target app's smali code.
- **Dependency Management:** All dependencies should be declared in `gradle/libs.versions.toml`.
- **Coding Style:** Follow the official Kotlin coding style (`kotlin.code.style = official`).
- **Versioning:** The project version is managed in `gradle.properties`.
- **Contribution:** All contributions should be made to the `dev` branch. Refer to `CONTRIBUTING.md` for detailed guidelines.

## Key Technologies

- **Kotlin:** Primary language for patch logic and Gradle configuration.
- **ReVanced Patcher:** The engine used to apply patches.
- **Smali/Dexlib2:** For low-level Dalvik bytecode manipulation.
- **Gradle:** Build system.
- **Semantic Release:** For automated versioning and releases.
- **Crowdin:** For community-driven localization.
