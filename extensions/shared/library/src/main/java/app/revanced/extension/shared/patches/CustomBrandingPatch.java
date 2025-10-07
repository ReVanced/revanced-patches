package app.revanced.extension.shared.patches;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import app.revanced.extension.shared.GmsCoreSupport;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

/**
 * Patch shared by YouTube and YT Music.
 */
@SuppressWarnings("unused")
public class CustomBrandingPatch {

    // Important: In the future, additional branding themes can be added but all existing and prior
    // themes cannot be removed or renamed.
    //
    // This is because if a user has a branding theme selected, then only that launch alias is enabled.
    // If a future update removes or renames that alias, then after updating the app is effectively
    // broken and it cannot be opened and not even clearing the app data will fix it.
    // In that situation the only fix is to completely uninstall and reinstall again.
    //
    // The most that can be done is to hide a theme from the UI and keep the alias with dummy data.
    public enum BrandingTheme {
        /**
         * Original unpatched icon. Must be first enum.
         */
        ORIGINAL("revanced_original"),
        ROUNDED("revanced_rounded"),
        MINIMAL("revanced_minimal"),
        SCALED("revanced_scaled"),
        /**
         * User provided custom icon. Must be the last enum.
         */
        CUSTOM("revanced_custom");

        public final String themeAlias;

        BrandingTheme(String themeAlias) {
            this.themeAlias = themeAlias;
        }

        private String packageAndNameIndexToClassAlias(String packageName, int appIndex) {
            if (appIndex <= 0) {
                throw new IllegalArgumentException("App index starts at index 1");
            }
            return packageName + '.' + themeAlias + '_' + appIndex;
        }
    }

    /**
     * Injection point.
     *
     * The number of app names available in the settings UI.
     */
    private static int numberOfCustomNames() {
        // Modified during patching.
        throw new IllegalStateException();
    }

    /**
     * Injection point.
     *
     * The total number of app name aliases, including dummy aliases.
     */
    private static int numberOfCustomNamesIncludingDummyAliases() {
        // Modified during patching.
        throw new IllegalStateException();
    }

    /**
     * Injection point.
     */
    public static void setBranding() {
        try {
            if (GmsCoreSupport.isPackageNameOriginal()) {
                Logger.printInfo(() -> "App is root mounted. Cannot dynamically change app icon");
                return;
            }

            Context context = Utils.getContext();
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            BrandingTheme selectedBranding = BaseSettings.CUSTOM_BRANDING_ICON.get();
            final int numberOfCustomNames = numberOfCustomNames();
            final int selectedNameIndex = BaseSettings.CUSTOM_BRANDING_NAME.get();
            ComponentName componentToEnable = null;
            ComponentName defaultComponent = null;
            List<ComponentName> componentsToDisable = new ArrayList<>();

            for (BrandingTheme theme : BrandingTheme.values()) {
                // Must always update all aliases including custom alias (last index).
                final int numberOfNamesIncludingDummies = numberOfCustomNamesIncludingDummyAliases();

                // App name indices starts at 1.
                for (int index = 1; index <= numberOfNamesIncludingDummies; index++) {
                    String aliasClass = theme.packageAndNameIndexToClassAlias(packageName, index);
                    ComponentName component = new ComponentName(packageName, aliasClass);
                    if (defaultComponent == null) {
                        // Default is always the first alias.
                        defaultComponent = component;
                    }

                    if (index == selectedNameIndex && theme == selectedBranding) {
                        componentToEnable = component;
                    } else {
                        componentsToDisable.add(component);
                    }
                }
            }

            if (componentToEnable == null) {
                // User imported a bad app name index value. Either the imported data
                // was corrupted, or they previously had custom name enabled and the app
                // no longer has a custom name specified.
                Utils.showToastLong("Custom branding reset");
                BaseSettings.CUSTOM_BRANDING_ICON.resetToDefault();
                BaseSettings.CUSTOM_BRANDING_NAME.resetToDefault();

                componentToEnable = defaultComponent;
                componentsToDisable.remove(defaultComponent);
            }

            for (ComponentName disable : componentsToDisable) {
                // Use info logging because if the alias status become corrupt the app cannot launch.
                Logger.printInfo(() -> "Disabling: " + disable.getClassName());
                pm.setComponentEnabledSetting(disable,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }

            ComponentName componentToEnableFinal = componentToEnable;
            Logger.printInfo(() -> "Enabling:  " + componentToEnableFinal.getClassName());
            pm.setComponentEnabledSetting(componentToEnable,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
        } catch (Exception ex) {
            Logger.printException(() -> "setBranding failure", ex);
        }
    }
}
