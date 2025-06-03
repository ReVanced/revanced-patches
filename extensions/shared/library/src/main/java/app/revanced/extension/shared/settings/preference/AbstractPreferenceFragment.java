package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.*;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.Setting;

@SuppressWarnings("deprecation")
public abstract class AbstractPreferenceFragment extends PreferenceFragment {

    /**
     * Indicates that if a preference changes,
     * to apply the change from the Setting to the UI component.
     */
    public static boolean settingImportInProgress;

    /**
     * Prevents recursive calls during preference <-> UI syncing from showing extra dialogs.
     */
    private static boolean updatingPreference;

    /**
     * Used to prevent showing reboot dialog, if user cancels a setting user dialog.
     */
    private static boolean showingUserDialogMessage;

    /**
     * Confirm and restart dialog button text and title.
     * Set by subclasses if Strings cannot be added as a resource.
     */
    @Nullable
    protected static String restartDialogButtonText, restartDialogTitle, confirmDialogTitle;

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            if (updatingPreference) {
                Logger.printDebug(() -> "Ignoring preference change as sync is in progress");
                return;
            }

            Setting<?> setting = Setting.getSettingFromPath(Objects.requireNonNull(str));
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            if (pref == null) {
                return;
            }
            Logger.printDebug(() -> "Preference changed: " + setting.key);

            if (!settingImportInProgress && !showingUserDialogMessage) {
                if (setting.userDialogMessage != null && !prefIsSetToDefault(pref, setting)) {
                    // Do not change the setting yet, to allow preserving whatever
                    // list/text value was previously set if it needs to be reverted.
                    showSettingUserDialogConfirmation(pref, setting);
                    return;
                } else if (setting.rebootApp) {
                    showRestartDialog(getContext());
                }
            }

            updatingPreference = true;
            // Apply 'Setting <- Preference', unless during importing when it needs to be 'Setting -> Preference'.
            // Updating here can can cause a recursive call back into this same method.
            updatePreference(pref, setting, true, settingImportInProgress);
            // Update any other preference availability that may now be different.
            updateUIAvailability();
            updatingPreference = false;
        } catch (Exception ex) {
            Logger.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    /**
     * Initialize this instance, and do any custom behavior.
     * <p>
     * To ensure all {@link Setting} instances are correctly synced to the UI,
     * it is important that subclasses make a call or otherwise reference their Settings class bundle
     * so all app specific {@link Setting} instances are loaded before this method returns.
     */
    protected void initialize() {
        String preferenceResourceName = BaseSettings.SHOW_MENU_ICONS.get()
                ? "revanced_prefs_icons"
                : "revanced_prefs";
        final var identifier = Utils.getResourceIdentifier(preferenceResourceName, "xml");
        if (identifier == 0) return;
        addPreferencesFromResource(identifier);

        PreferenceScreen screen = getPreferenceScreen();
        Utils.sortPreferenceGroups(screen);
        Utils.setPreferenceTitlesToMultiLineIfNeeded(screen);
    }

    private static Dialog createCustomDialog(Context context, String title, String message, String okButtonText,
                                             Runnable onOkClick, Runnable onCancelClick) {
        Logger.printDebug(() -> "Creating custom dialog with title: " + title);

        // Use a theme to remove default dialog styling.
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove default title bar.

        // Create main layout
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Preset size constants.
        final int dip4 = dipToPixels(4);
        final int dip8 = dipToPixels(8);
        final int dip28 = dipToPixels(28); // Padding for mainLayout.
        final int dip36 = dipToPixels(36); // Height for buttons.

        mainLayout.setPadding(dip28, dip28, dip28, dip28);
        // Set rounded rectangle background with black color.
        ShapeDrawable mainBackground = new ShapeDrawable(new RoundRectShape(
                createCornerRadii(28), null, null));
        mainBackground.getPaint().setColor(Utils.isDarkModeEnabled()
                ? ReVancedAboutPreference.getDarkColor()
                : ReVancedAboutPreference.getLightColor());
        mainLayout.setBackground(mainBackground);

        // Title.
        if (!TextUtils.isEmpty(title)) {
            TextView titleView = new TextView(context);
            titleView.setText(title);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextSize(20);
            titleView.setTextColor(Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK);
            titleView.setPadding(0, 0, 0, dip8);
            mainLayout.addView(titleView);
        }

        // Message.
        TextView messageView = new TextView(context);
        messageView.setText(message != null ? message : "");
        messageView.setTextSize(14);
        messageView.setTextColor(Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK);
        messageView.setPadding(0, 0, 0, dip8);
        mainLayout.addView(messageView);

        // Button container.
        LinearLayout buttonContainer = new LinearLayout(context);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonContainerParams.setMargins(0, dip8, 0, 0);
        buttonContainer.setLayoutParams(buttonContainerParams);
        buttonContainer.setGravity(Gravity.CENTER);

        // Cancel Button.
        if (onCancelClick != null) {
            Button cancelButton = new Button(context, null, 0);
            cancelButton.setText(context.getString(android.R.string.cancel));
            cancelButton.setTextSize(14);
            cancelButton.setAllCaps(false); // Normal case text.
            cancelButton.setSingleLine(true); // Ensure single line for truncation.
            cancelButton.setEllipsize(TextUtils.TruncateAt.END); // Truncate with ellipsis.
            cancelButton.setGravity(Gravity.CENTER); // Center text vertically and horizontally.
            ShapeDrawable cancelBackground = new ShapeDrawable(new RoundRectShape(
                    createCornerRadii(20), null, null));
            final int cancelButtonBackgroundColor = Utils.isDarkModeEnabled()
                    ? Utils.adjustColorBrightness(Color.BLACK, 1.10f)
                    : Utils.adjustColorBrightness(Color.WHITE, 0.95f);
            cancelBackground.getPaint().setColor(cancelButtonBackgroundColor);
            cancelButton.setBackground(cancelBackground);
            cancelButton.setTextColor(Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK);
            cancelButton.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                    0, // Weight will control width.
                    dip36 // Exact height of 36dp.
            );
            cancelParams.weight = 1; // Equal weight for horizontal stretching
            cancelParams.setMargins(0, 0, dip4, 0); // 4dp right margin (half of 8dp total).
            cancelButton.setLayoutParams(cancelParams);
            cancelButton.setOnClickListener(v -> {
                onCancelClick.run();
                dialog.dismiss();
            });
            buttonContainer.addView(cancelButton);
        }

        // OK Button.
        Button okButton = new Button(context, null, 0);
        String okDialogButtonText = okButtonText != null ? okButtonText : context.getString(android.R.string.ok);
        okButton.setText(okDialogButtonText);
        okButton.setTextSize(14);
        okButton.setAllCaps(false); // Normal case text.
        okButton.setSingleLine(true); // Ensure single line for truncation.
        okButton.setEllipsize(TextUtils.TruncateAt.END); // Truncate with ellipsis.
        okButton.setGravity(Gravity.CENTER); // Center text vertically and horizontally.
        ShapeDrawable okBackground = new ShapeDrawable(new RoundRectShape(
                createCornerRadii(20), null, null));
        okBackground.getPaint().setColor(Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK);
        okButton.setBackground(okBackground);
        okButton.setTextColor(Utils.isDarkModeEnabled() ? Color.BLACK : Color.WHITE);
        okButton.setPadding(0, 0, 0, 0); // Remove all padding to control height precisely.
        LinearLayout.LayoutParams okParams = new LinearLayout.LayoutParams(
                0, // Weight will control width
                dip36 // Exact height of 36dp
        );
        okParams.weight = 1; // Equal weight for horizontal stretching
        okParams.setMargins(dip4, 0, 0, 0); // 4dp left margin (half of 8dp total).
        okButton.setLayoutParams(okParams);
        okButton.setOnClickListener(v -> {
            if (onOkClick != null) {
                onOkClick.run();
            }
            dialog.dismiss();
        });
        buttonContainer.addView(okButton);

        mainLayout.addView(buttonContainer);
        dialog.setContentView(mainLayout);

        // Set dialog window attributes.
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            int portraitWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                portraitWidth = (int) Math.min(
                        portraitWidth,
                        context.getResources().getDisplayMetrics().heightPixels * 0.9);
            }
            params.width = portraitWidth;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(null); // Remove default dialog background.
        }

        return dialog;
    }

    /**
     * Creates an array of corner radii for a rounded rectangle shape.
     *
     * @param context The context to convert dp to pixels.
     * @param dp The radius in density-independent pixels (dp) to apply to all corners.
     * @return An array of eight float values representing the corner radii
     * (top-left, top-right, bottom-right, bottom-left).
     */
    private static float[] createCornerRadii(float dp) {
        final float radius = dipToPixels(dp);
        return new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
    }

    private void showSettingUserDialogConfirmation(Preference pref, Setting<?> setting) {
        Utils.verifyOnMainThread();

        final var context = getContext();
        if (confirmDialogTitle == null) {
            confirmDialogTitle = str("revanced_settings_confirm_user_dialog_title");
        }

        showingUserDialogMessage = true;

        Dialog dialog = createCustomDialog(context,
                confirmDialogTitle,
                Objects.requireNonNull(setting.userDialogMessage).toString(),
                null,
                () -> {
                    // User confirmed, save to the Setting.
                    updatePreference(pref, setting, true, false);

                    // Update availability of other preferences that may be changed.
                    updateUIAvailability();

                    if (setting.rebootApp) {
                        showRestartDialog(context);
                    }
                },
                () -> updatePreference(pref, setting, true, true) // Restore whatever the setting was before the change.
        );

        dialog.setOnDismissListener(d -> showingUserDialogMessage = false);
        dialog.show();
    }

    /**
     * Updates all Preferences values and their availability using the current values in {@link Setting}.
     */
    protected void updateUIToSettingValues() {
        updatePreferenceScreen(getPreferenceScreen(), true, true);
    }

    /**
     * Updates Preferences availability only using the status of {@link Setting}.
     */
    protected void updateUIAvailability() {
        updatePreferenceScreen(getPreferenceScreen(), false, false);
    }

    /**
     * @return If the preference is currently set to the default value of the Setting.
     */
    protected boolean prefIsSetToDefault(Preference pref, Setting<?> setting) {
        Object defaultValue = setting.defaultValue;
        if (pref instanceof SwitchPreference switchPref) {
            return switchPref.isChecked() == (Boolean) defaultValue;
        }
        String defaultValueString = defaultValue.toString();
        if (pref instanceof EditTextPreference editPreference) {
            return editPreference.getText().equals(defaultValueString);
        }
        if (pref instanceof ListPreference listPref) {
            return listPref.getValue().equals(defaultValueString);
        }

        throw new IllegalStateException("Must override method to handle "
                + "preference type: " + pref.getClass());
    }

    /**
     * Syncs all UI Preferences to any {@link Setting} they represent.
     */
    private void updatePreferenceScreen(@NonNull PreferenceGroup group,
                                        boolean syncSettingValue,
                                        boolean applySettingToPreference) {
        // Alternatively this could iterate thru all Settings and check for any matching Preferences,
        // but there are many more Settings than UI preferences so it's more efficient to only check
        // the Preferences.
        for (int i = 0, prefCount = group.getPreferenceCount(); i < prefCount; i++) {
            Preference pref = group.getPreference(i);
            if (pref instanceof PreferenceGroup subGroup) {
                updatePreferenceScreen(subGroup, syncSettingValue, applySettingToPreference);
            } else if (pref.hasKey()) {
                String key = pref.getKey();
                Setting<?> setting = Setting.getSettingFromPath(key);

                if (setting != null) {
                    updatePreference(pref, setting, syncSettingValue, applySettingToPreference);
                } else if (BaseSettings.DEBUG.get() && (pref instanceof SwitchPreference
                        || pref instanceof EditTextPreference || pref instanceof ListPreference)) {
                    // Probably a typo in the patches preference declaration.
                    Logger.printException(() -> "Preference key has no setting: " + key);
                }
            }
        }
    }

    /**
     * Handles syncing a UI Preference with the {@link Setting} that backs it.
     * If needed, subclasses can override this to handle additional UI Preference types.
     *
     * @param applySettingToPreference If true, then apply {@link Setting} -> Preference.
     *                                 If false, then apply {@link Setting} <- Preference.
     */
    protected void syncSettingWithPreference(@NonNull Preference pref,
                                             @NonNull Setting<?> setting,
                                             boolean applySettingToPreference) {
        if (pref instanceof SwitchPreference switchPref) {
            BooleanSetting boolSetting = (BooleanSetting) setting;
            if (applySettingToPreference) {
                switchPref.setChecked(boolSetting.get());
            } else {
                BooleanSetting.privateSetValue(boolSetting, switchPref.isChecked());
            }
        } else if (pref instanceof EditTextPreference editPreference) {
            if (applySettingToPreference) {
                editPreference.setText(setting.get().toString());
            } else {
                Setting.privateSetValueFromString(setting, editPreference.getText());
            }
        } else if (pref instanceof ListPreference listPref) {
            if (applySettingToPreference) {
                listPref.setValue(setting.get().toString());
            } else {
                Setting.privateSetValueFromString(setting, listPref.getValue());
            }
            updateListPreferenceSummary(listPref, setting);
        } else {
            Logger.printException(() -> "Setting cannot be handled: " + pref.getClass() + ": " + pref);
        }
    }

    /**
     * Updates a UI Preference with the {@link Setting} that backs it.
     *
     * @param syncSetting If the UI should be synced {@link Setting} <-> Preference
     * @param applySettingToPreference If true, then apply {@link Setting} -> Preference.
     *                                 If false, then apply {@link Setting} <- Preference.
     */
    private void updatePreference(@NonNull Preference pref, @NonNull Setting<?> setting,
                                  boolean syncSetting, boolean applySettingToPreference) {
        if (!syncSetting && applySettingToPreference) {
            throw new IllegalArgumentException();
        }

        if (syncSetting) {
            syncSettingWithPreference(pref, setting, applySettingToPreference);
        }

        updatePreferenceAvailability(pref, setting);
    }

    protected void updatePreferenceAvailability(@NonNull Preference pref, @NonNull Setting<?> setting) {
        pref.setEnabled(setting.isAvailable());
    }

    protected void updateListPreferenceSummary(ListPreference listPreference, Setting<?> setting) {
        String objectStringValue = setting.get().toString();
        final int entryIndex = listPreference.findIndexOfValue(objectStringValue);
        if (entryIndex >= 0) {
            listPreference.setSummary(listPreference.getEntries()[entryIndex]);
        } else {
            // Value is not an available option.
            // User manually edited import data, or options changed and current selection is no longer available.
            // Still show the value in the summary, so it's clear that something is selected.
            listPreference.setSummary(objectStringValue);
        }
    }

    public static void showRestartDialog(Context context) {
        Utils.verifyOnMainThread();
        if (restartDialogTitle == null) {
            restartDialogTitle = str("revanced_settings_restart_title");
        }
        if (restartDialogButtonText == null) {
            restartDialogButtonText = str("revanced_settings_restart");
        }

        Dialog dialog = createCustomDialog(context,
                restartDialogTitle,
                null,
                restartDialogButtonText,
                () -> Utils.restartApp(context),
                () -> {} // Cancel action just dismisses the dialog
        );

        dialog.show();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(Setting.preferences.name);

            // Must initialize before adding change listener,
            // otherwise the syncing of Setting -> UI
            // causes a callback to the listener even though nothing changed.
            initialize();
            updateUIToSettingValues();

            preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        } catch (Exception ex) {
            Logger.printException(() -> "onCreate() failure", ex);
        }
    }

    @Override
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
