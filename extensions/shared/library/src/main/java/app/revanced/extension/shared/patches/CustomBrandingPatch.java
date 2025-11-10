package app.revanced.extension.shared.patches;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
         * Original unpatched icon.
         */
        ORIGINAL,
        ROUNDED,
        MINIMAL,
        SCALED,
        /**
         * User provided custom icon.
         */
        CUSTOM;

        private String packageAndNameIndexToClassAlias(String packageName, int appIndex) {
            if (appIndex <= 0) {
                throw new IllegalArgumentException("App index starts at index 1");
            }
            return packageName + ".revanced_" + name().toLowerCase(Locale.US) + '_' + appIndex;
        }
    }

    private static final int notificationSmallIcon;

    static {
        BrandingTheme branding = BaseSettings.CUSTOM_BRANDING_ICON.get();
        if (branding == BrandingTheme.ORIGINAL) {
            notificationSmallIcon = 0;
        } else {
            // Original icon is quantum_ic_video_youtube_white_24
            String iconName = "revanced_notification_icon";
            if (branding == BrandingTheme.CUSTOM) {
                iconName += "_custom";
            }

            notificationSmallIcon = Utils.getResourceIdentifier(iconName, "drawable");
            if (notificationSmallIcon == 0) {
                Logger.printException(() -> "Could not load notification small icon");
            }
        }
    }

    /**
     * Injection point.
     */
    public static View getLottieViewOrNull(View lottieStartupView) {
        if (BaseSettings.CUSTOM_BRANDING_ICON.get() == BrandingTheme.ORIGINAL) {
            return lottieStartupView;
        }

        return null;
    }

    /**
     * Injection point.
     */
    public static void setNotificationIcon(Notification.Builder builder) {
        try {
            if (notificationSmallIcon != 0) {
                builder.setSmallIcon(notificationSmallIcon)
                        .setColor(Color.TRANSPARENT); // Remove YT red tint.
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setNotificationIcon failure", ex);
        }
    }

    /**
     * Injection point.
     *
     * The total number of app name aliases, including dummy aliases.
     */
    private static int numberOfPresetAppNames() {
        // Modified during patching.
        throw new IllegalStateException();
    }

    /**
     * Injection point.
     */
    @SuppressWarnings("ConstantConditions")
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
            final int selectedNameIndex = BaseSettings.CUSTOM_BRANDING_NAME.get();
            ComponentName componentToEnable = null;
            ComponentName defaultComponent = null;
            List<ComponentName> componentsToDisable = new ArrayList<>();

            for (BrandingTheme theme : BrandingTheme.values()) {
                // Must always update all aliases including custom alias (last index).
                final int numberOfPresetAppNames = numberOfPresetAppNames();

                // App name indices starts at 1.
                for (int index = 1; index <= numberOfPresetAppNames; index++) {
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
                pm.setComponentEnabledSetting(disable,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }

            // Use info logging because if the alias status become corrupt the app cannot launch.
            ComponentName componentToEnableFinal = componentToEnable;
            Logger.printInfo(() -> "Enabling:  " + componentToEnableFinal.getClassName());

            pm.setComponentEnabledSetting(componentToEnable,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
        } catch (Exception ex) {
            Logger.printException(() -> "setBranding failure", ex);
        }
    }
}
