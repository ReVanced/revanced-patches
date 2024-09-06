package app.revanced.integrations.shared.checks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.shared.checks.Check.debugAlwaysShowWarning;
import static app.revanced.integrations.shared.checks.PatchInfo.Build.*;
import static app.revanced.integrations.shared.checks.PatchInfo.PATCH_TIME;

/**
 * This class is used to check if the app was patched by the user
 * and not downloaded pre-patched, because pre-patched apps are difficult to trust.
 * <br>
 * Various indicators help to detect if the app was patched by the user.
 */
@SuppressWarnings("unused")
public final class CheckEnvironmentPatch {
    private static final boolean DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG = debugAlwaysShowWarning();

    private enum InstallationType {
        /**
         * CLI patching, manual installation of a previously patched using adb,
         * or root installation if stock app is first installed using adb.
         */
        ADB((String) null),
        ROOT_MOUNT_ON_APP_STORE("com.android.vending"),
        MANAGER("app.revanced.manager.flutter",
                "app.revanced.manager",
                "app.revanced.manager.debug");

        @Nullable
        static InstallationType installTypeFromPackageName(@Nullable String packageName) {
            for (InstallationType type : values()) {
                for (String installPackageName : type.packageNames) {
                    if (Objects.equals(installPackageName, packageName)) {
                        return type;
                    }
                }
            }

            return null;
        }

        /**
         * Array elements can be null.
         */
        final String[] packageNames;

        InstallationType(String... packageNames) {
            this.packageNames = packageNames;
        }
    }

    /**
     * Check if the app is installed by the manager, the app store, or through adb/CLI.
     * <br>
     * Does not conclusively
     * If the app is installed by the manager or the app store, it is likely, the app was patched using the manager,
     * or installed manually via ADB (in the case of ReVanced CLI for example).
     * <br>
     * If the app is not installed by the manager or the app store, then the app was likely downloaded pre-patched
     * and installed by the browser or another unknown app.
     */
    private static class CheckExpectedInstaller extends Check {
        @Nullable
        InstallationType installerFound;

        @NonNull
        @Override
        protected Boolean check() {
            final var context = Utils.getContext();

            final var installerPackageName =
                    context.getPackageManager().getInstallerPackageName(context.getPackageName());

            Logger.printInfo(() -> "Installed by: " + installerPackageName);

            installerFound = InstallationType.installTypeFromPackageName(installerPackageName);
            final boolean passed = (installerFound != null);

            Logger.printInfo(() -> passed
                    ? "Apk was not installed from an unknown source"
                    : "Apk was installed from an unknown source");

            return passed;
        }

        @Override
        protected String failureReason() {
            return str("revanced_check_environment_manager_not_expected_installer");
        }

        @Override
        public int uiSortingValue() {
            return -100; // Show first.
        }
    }

    /**
     * Check if the build properties are the same as during the patch.
     * <br>
     * If the build properties are the same as during the patch, it is likely, the app was patched on the same device.
     * <br>
     * If the build properties are different, the app was likely downloaded pre-patched or patched on another device.
     */
    private static class CheckWasPatchedOnSameDevice extends Check {
        @SuppressLint({"NewApi", "HardwareIds"})
        @Override
        protected Boolean check() {
            if (PATCH_BOARD.isEmpty()) {
                // Did not patch with Manager, and cannot conclusively say where this was from.
                Logger.printInfo(() -> "APK does not contain a hardware signature and cannot compare to current device");
                return null;
            }

            //noinspection deprecation
            final var passed = buildFieldEqualsHash("BOARD", Build.BOARD, PATCH_BOARD) &
                    buildFieldEqualsHash("BOOTLOADER", Build.BOOTLOADER, PATCH_BOOTLOADER) &
                    buildFieldEqualsHash("BRAND", Build.BRAND, PATCH_BRAND) &
                    buildFieldEqualsHash("CPU_ABI", Build.CPU_ABI, PATCH_CPU_ABI) &
                    buildFieldEqualsHash("CPU_ABI2", Build.CPU_ABI2, PATCH_CPU_ABI2) &
                    buildFieldEqualsHash("DEVICE", Build.DEVICE, PATCH_DEVICE) &
                    buildFieldEqualsHash("DISPLAY", Build.DISPLAY, PATCH_DISPLAY) &
                    buildFieldEqualsHash("FINGERPRINT", Build.FINGERPRINT, PATCH_FINGERPRINT) &
                    buildFieldEqualsHash("HARDWARE", Build.HARDWARE, PATCH_HARDWARE) &
                    buildFieldEqualsHash("HOST", Build.HOST, PATCH_HOST) &
                    buildFieldEqualsHash("ID", Build.ID, PATCH_ID) &
                    buildFieldEqualsHash("MANUFACTURER", Build.MANUFACTURER, PATCH_MANUFACTURER) &
                    buildFieldEqualsHash("MODEL", Build.MODEL, PATCH_MODEL) &
                    buildFieldEqualsHash("ODM_SKU", Build.ODM_SKU, PATCH_ODM_SKU) &
                    buildFieldEqualsHash("PRODUCT", Build.PRODUCT, PATCH_PRODUCT) &
                    buildFieldEqualsHash("RADIO", Build.RADIO, PATCH_RADIO) &
                    buildFieldEqualsHash("SKU", Build.SKU, PATCH_SKU) &
                    buildFieldEqualsHash("SOC_MANUFACTURER", Build.SOC_MANUFACTURER, PATCH_SOC_MANUFACTURER) &
                    buildFieldEqualsHash("SOC_MODEL", Build.SOC_MODEL, PATCH_SOC_MODEL) &
                    buildFieldEqualsHash("TAGS", Build.TAGS, PATCH_TAGS) &
                    buildFieldEqualsHash("TYPE", Build.TYPE, PATCH_TYPE) &
                    buildFieldEqualsHash("USER", Build.USER, PATCH_USER);

            Logger.printInfo(() -> passed
                    ? "Device hardware signature matches current device"
                    : "Device hardware signature does not match current device");

            return passed;
        }

        @Override
        protected String failureReason() {
            return str("revanced_check_environment_not_same_patching_device");
        }

        @Override
        public int uiSortingValue() {
            return 0; // Show in the middle.
        }
    }

    /**
     * Check if the app was installed within the last 30 minutes after being patched.
     * <br>
     * If the app was installed within the last 30 minutes, it is likely, the app was patched by the user.
     * <br>
     * If the app was installed much later than the patch time, it is likely the app was
     * downloaded pre-patched or the user waited too long to install the app.
     */
    private static class CheckIsNearPatchTime extends Check {
        /**
         * How soon after patching the app must be first launched.
         */
        static final int THRESHOLD_FOR_PATCHING_RECENTLY = 30 * 60 * 1000;  // 30 minutes.

        /**
         * How soon after installation or updating the app to check the patch time.
         * If the install/update is older than this, this entire check is ignored
         * to prevent showing any errors if the user clears the app data after installation.
         */
        static final int THRESHOLD_FOR_RECENT_INSTALLATION = 12 * 60 * 60 * 1000;  // 12 hours.

        static final long DURATION_SINCE_PATCHING = System.currentTimeMillis() - PATCH_TIME;

        @Override
        protected Boolean check() {
            Logger.printInfo(() -> "Installed: " + (DURATION_SINCE_PATCHING / 1000) + " seconds after patching");

            // Also verify patched time is not in the future.
            if (DURATION_SINCE_PATCHING < 0) {
                // Patch time is in the future and clearly wrong.
                return false;
            }

            if (DURATION_SINCE_PATCHING < THRESHOLD_FOR_PATCHING_RECENTLY) {
                // App is recently patched and this installation is new or recently updated.
                return true;
            }

            // Verify the app install/update is recent,
            // to prevent showing errors if the user later clears the app data.
            try {
                Context context = Utils.getContext();
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

                // Duration since initial install or last update, which ever is sooner.
                final long durationSinceInstallUpdate = System.currentTimeMillis() - packageInfo.lastUpdateTime;
                Logger.printInfo(() -> "App was installed/updated: "
                        + (durationSinceInstallUpdate / (60 * 60 * 1000)) + " hours ago");

                if (durationSinceInstallUpdate > THRESHOLD_FOR_RECENT_INSTALLATION) {
                    Logger.printInfo(() -> "Ignoring install time check since install/update was over "
                            + THRESHOLD_FOR_RECENT_INSTALLATION + " hours ago");
                    return null;
                }
            } catch (PackageManager.NameNotFoundException ex) {
                Logger.printException(() -> "Package name not found exception", ex); // Will never happen.
            }

            // Was patched between 30 minutes and 12 hours ago.
            // This can only happen if someone installs the app then waits 30+ minutes to launch,
            // or they clear the app data within 12 hours after installation.
            return false;
        }

        @Override
        protected String failureReason() {
            if (DURATION_SINCE_PATCHING < 0) {
                // Could happen if the user has their device clock incorrectly set in the past,
                // but assume that isn't the case and the apk was patched on a device with the wrong system time.
                return str("revanced_check_environment_not_near_patch_time_invalid");
            }

            // If patched over 1 day ago, show how old this pre-patched apk is.
            // Showing the age can help convey it's better to patch yourself and know it's the latest.
            final long oneDay = 24 * 60 * 60 * 1000;
            final long daysSincePatching = DURATION_SINCE_PATCHING / oneDay;
            if (daysSincePatching > 1) { // Use over 1 day to avoid singular vs plural strings.
                return str("revanced_check_environment_not_near_patch_time_days", daysSincePatching);
            }

            return str("revanced_check_environment_not_near_patch_time");
        }

        @Override
        public int uiSortingValue() {
            return 100; // Show last.
        }
    }

    /**
     * Injection point.
     */
    public static void check(Activity context) {
        // If the warning was already issued twice, or if the check was successful in the past,
        // do not run the checks again.
        if (!Check.shouldRun() && !DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
            Logger.printDebug(() -> "Environment checks are disabled");
            return;
        }

        Utils.runOnBackgroundThread(() -> {
            try {
                Logger.printInfo(() -> "Running environment checks");
                List<Check> failedChecks = new ArrayList<>();

                CheckWasPatchedOnSameDevice sameHardware = new CheckWasPatchedOnSameDevice();
                Boolean hardwareCheckPassed = sameHardware.check();
                if (hardwareCheckPassed != null) {
                    if (hardwareCheckPassed && !DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                        // Patched on the same device using Manager,
                        // and no further checks are needed.
                        Check.disableForever();
                        return;
                    }

                    failedChecks.add(sameHardware);
                }

                CheckIsNearPatchTime nearPatchTime = new CheckIsNearPatchTime();
                Boolean timeCheckPassed = nearPatchTime.check();
                if (timeCheckPassed != null) {
                    if (timeCheckPassed && !DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                        if (failedChecks.isEmpty()) {
                            // Recently patched and installed. No further checks are needed.
                            // Stopping here also prevents showing warnings if patching and installing with Termux.
                            Check.disableForever();
                            return;
                        }
                    } else {
                        failedChecks.add(nearPatchTime);
                    }
                }

                CheckExpectedInstaller installerCheck = new CheckExpectedInstaller();
                // If the installer package is Manager but this code is reached,
                // that means it must not be the right Manager otherwise the hardware hash
                // signatures would be present and this check would not have run.
                final boolean isManagerInstall = installerCheck.installerFound == InstallationType.MANAGER;
                if (!installerCheck.check() || isManagerInstall) {
                    failedChecks.add(installerCheck);

                    if (isManagerInstall) {
                        // If using Manager and reached here, then this must
                        // have been patched on a different device.
                        failedChecks.add(sameHardware);
                    }
                }

                if (DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                    // Show all failures for debugging layout.
                    failedChecks = Arrays.asList(
                            sameHardware,
                            nearPatchTime,
                            installerCheck
                    );
                }

                if (failedChecks.isEmpty()) {
                    Check.disableForever();
                    return;
                }

                //noinspection ComparatorCombinators
                Collections.sort(failedChecks, (o1, o2) -> o1.uiSortingValue() - o2.uiSortingValue());

                Check.issueWarning(
                        context,
                        failedChecks
                );
            } catch (Exception ex) {
                Logger.printException(() -> "check failure", ex);
            }
        });
    }

    private static boolean buildFieldEqualsHash(String buildFieldName, String buildFieldValue, @Nullable String hash) {
        try {
            final var sha1 = MessageDigest.getInstance("SHA-1")
                    .digest(buildFieldValue.getBytes(StandardCharsets.UTF_8));

            // Must be careful to use same base64 encoding Kotlin uses.
            String runtimeHash = new String(Base64.encode(sha1, Base64.NO_WRAP), StandardCharsets.ISO_8859_1);
            final boolean equals = runtimeHash.equals(hash);
            if (!equals) {
                Logger.printInfo(() -> "Hashes do not match. " + buildFieldName + ": '" + buildFieldValue
                        + "' runtimeHash: '" + runtimeHash + "' patchTimeHash: '" + hash + "'");
            }

            return equals;
        } catch (NoSuchAlgorithmException ex) {
            Logger.printException(() -> "buildFieldEqualsHash failure", ex); // Will never happen.

            return false;
        }
    }
}
