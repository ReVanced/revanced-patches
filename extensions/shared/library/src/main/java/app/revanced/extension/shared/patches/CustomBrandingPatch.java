package app.revanced.extension.shared.patches;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

/**
 * Patch shared by YouTube and YT Music.
 */
@SuppressWarnings("unused")
public class CustomBrandingPatch {

    public enum BrandingTheme {
        // Original must be first.
        ORIGINAL("revanced_original"),
        ROUNDED("revanced_rounded"),
        MINIMAL("revanced_minimal"),
        SCALED("revanced_scaled"),
        /** User provided custom icon. */
        CUSTOM("revanced_custom");

        public final String themeAlias;

        BrandingTheme(String themeAlias) {
            this.themeAlias = themeAlias;
        }
    }

    /**
     * Injection point.
     */
    private static boolean customIconIncluded() {
        // Modified during patching.
        throw new IllegalStateException();
    }

    /**
     * Injection point.
     */
    private static int numberOfCustomNames() {
        // Modified during patching.
        throw new IllegalStateException();
    }

    private static ComponentName createComponentName(BrandingTheme theme, String packageName, int index) {
        return new ComponentName(packageName,
                packageName + '.' + theme.themeAlias + '_' + index);
    }

    /**
     * Injection point.
     */
    public static void setBrandingIcon() {
        try {
            Context context = Utils.getContext();
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            final int numberOfCustomNames = numberOfCustomNames();
            BrandingTheme selectedBranding = BaseSettings.CUSTOM_BRANDING_ICON.get();
            int selectedNameIndex = BaseSettings.CUSTOM_BRANDING_NAME.get();
            if (selectedNameIndex <= 0 || selectedNameIndex > numberOfCustomNames) {
                // User imported a bad app name index value. Either the imported data
                // was corrupted, or they previously had custom name enabled and the app
                // no longer has a custom name specified.
                Utils.showToastLong("Custom branding reset");
                selectedNameIndex = BaseSettings.CUSTOM_BRANDING_NAME.resetToDefault();
            }

            final boolean customIconIncluded = customIconIncluded();
            ComponentName componentToEnable = null;
            ComponentName defaultComponent = null;
            List<ComponentName> componentsToDisable = new ArrayList<>();

            for (BrandingTheme theme : BrandingTheme.values()) {
                // App name indices starts at 1.
                for (int index = 1; index <= numberOfCustomNames; index++) {
                    if (!customIconIncluded && theme == BrandingTheme.CUSTOM) {
                        continue;
                    }

                    String aliasClass = packageName + '.' + theme.themeAlias + '_' + index;
                    ComponentName component = new ComponentName(packageName, aliasClass);
                    if (defaultComponent == null) {
                        // Default is always the first alias.
                        defaultComponent = component;
                    }

                    if (index == selectedNameIndex && theme == selectedBranding) {
                        if (componentToEnable != null) {
                            // Should never happen.
                            ComponentName componentToEnableFinal = componentToEnable;
                            Logger.printException(() -> "Found duplicate alias: "
                                    + componentToEnableFinal.getClassName());
                            componentsToDisable.add(componentToEnable);
                        }
                        componentToEnable = component;
                    } else {
                        componentsToDisable.add(component);
                    }
                }
            }

            if (componentToEnable == null) {
                // Should never be reached, because the branding icon enum
                // resets itself the saved enum type is invalid or no longer exists,
                // and a bad name index is already handled above.
                Logger.printException(() -> "Could not find alias to enable");
                BaseSettings.CUSTOM_BRANDING_ICON.resetToDefault();
                BaseSettings.CUSTOM_BRANDING_NAME.resetToDefault();

                componentToEnable = defaultComponent;
                componentsToDisable.remove(defaultComponent);
            }

            for (ComponentName disable : componentsToDisable) {
                // Use info logging because if the aliases state become corrupt the app cannot launch.
                Logger.printInfo(() -> "Disabling: " + disable.getClassName());
                pm.setComponentEnabledSetting(disable,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }

            ComponentName componentToEnableFinal = componentToEnable;
            Logger.printInfo(() -> "Enabling:  " + componentToEnableFinal.getClassName());
            pm.setComponentEnabledSetting(componentToEnable,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
        } catch (Exception ex) {
            Logger.printException(() -> "setBrandingIcon failure", ex);
        }
    }
}
