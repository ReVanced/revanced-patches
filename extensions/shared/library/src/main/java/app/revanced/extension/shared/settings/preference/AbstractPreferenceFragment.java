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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    protected static String restartDialogButtonText, restartDialogTitle, confirmDialogTitle, restartDialogMessage;

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
            // Updating here can cause a recursive call back into this same method.
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

    protected static Pair<Dialog, LinearLayout> createCustomDialog(
            Context context,String title, String message,
            String okButtonText, Runnable onOkClick,
            Runnable onCancelClick,
            @Nullable String neutralButtonText, @Nullable Runnable onNeutralClick
    ) {
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
        final int dip16 = dipToPixels(16);
        final int dip28 = dipToPixels(28); // Padding for mainLayout.
        final int dip36 = dipToPixels(36); // Height for buttons.

        mainLayout.setPadding(dip28, dip16, dip28, dip28);
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
            titleView.setTextSize(18);
            titleView.setTextColor(Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK);
            titleView.setPadding(0, 0, 0, dip8);
            titleView.setGravity(Gravity.CENTER);
            // Set layout parameters to match parent width and wrap content height.
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            titleView.setLayoutParams(layoutParams);
            mainLayout.addView(titleView);
        }

        // Message.
        TextView messageView = new TextView(context);
        messageView.setText(message != null ? message : "");
        messageView.setTextSize(16);
        messageView.setTextColor(Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK);
        messageView.setPadding(0, dip8, 0, dip16);
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

        if (neutralButtonText != null && onNeutralClick != null) {
            addButton(buttonContainer, context, neutralButtonText, onNeutralClick, false,false, true, dialog);
        }

        if (onCancelClick != null) {
            addButton(buttonContainer, context, context.getString(android.R.string.cancel),
                    onCancelClick, false, neutralButtonText != null, false, dialog);
        }

        addButton(buttonContainer, context,
                okButtonText != null ? okButtonText : context.getString(android.R.string.ok),
                onOkClick, true, neutralButtonText != null, false, dialog);

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

        return new Pair<>(dialog, mainLayout);
    }

    /**
     * Adds a styled button to a dialog's button container with specified text, click behavior, and appearance.
     * The button's background and text color adapt to the system's dark mode setting, and margins are adjusted
     * based on the button type and presence of a neutral button.
     *
     * @param buttonContainer The LinearLayout to which the button will be added.
     * @param context         The Context used to create the button and access resources.
     * @param buttonText      The text to display on the button.
     * @param onClick         The Runnable to execute when the button is clicked, or null if no action is needed.
     * @param isOkButton      True if the button is the OK button, affecting its background and text color.
     * @param isCancelButton  True if the button is the Cancel button, affecting its background and text color.
     * @param isNeutralButton True if a neutral button exists, affecting margin settings for non-OK buttons.
     * @param dialog          The Dialog to dismiss when the button is clicked.
     */
    protected static void addButton(
            LinearLayout buttonContainer,
            Context context,
            String buttonText,
            Runnable onClick,
            boolean isOkButton,
            boolean isCancelButton,
            boolean isNeutralButton,
            Dialog dialog) {

        Button button = new Button(context, null, 0);
        button.setText(buttonText);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setGravity(Gravity.CENTER);

        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(createCornerRadii(20), null, null));
        int backgroundColor = isOkButton
                ? (Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK)
                : Utils.isDarkModeEnabled()
                ? Utils.adjustColorBrightness(Color.BLACK, 1.10f)
                : Utils.adjustColorBrightness(Color.WHITE, 0.95f);
        background.getPaint().setColor(backgroundColor);
        button.setBackground(background);

        button.setTextColor(Utils.isDarkModeEnabled()
                ? (isOkButton ? Color.BLACK : Color.WHITE)
                : (isOkButton ? Color.WHITE : Color.BLACK));
        button.setPadding(0, 0, 0, 0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dipToPixels(36));
        params.weight = 1;
        if (isOkButton) {
            params.setMargins(dipToPixels(4), 0, 0, 0);
        }
        if (isCancelButton) {
            params.setMargins(0, 0, dipToPixels(4), 0);
        }
        if (isNeutralButton) {
            params.setMargins(dipToPixels(8), 0, 0, 0);
        }
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.run();
            }
            dialog.dismiss();
        });

        buttonContainer.addView(button);
    }

    /**
     * Creates an array of corner radii for a rounded rectangle shape.
     *
     * @param dp The radius in density-independent pixels (dp) to apply to all corners.
     * @return An array of eight float values representing the corner radii
     * (top-left, top-right, bottom-right, bottom-left).
     */
    protected static float[] createCornerRadii(float dp) {
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

        Pair<Dialog, LinearLayout> dialogPair = createCustomDialog(context,
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
                () -> updatePreference(pref, setting, true, true), // Restore whatever the setting was before the change.
                null, // No neutral button for this dialog
                null
        );

        dialogPair.first.setOnDismissListener(d -> showingUserDialogMessage = false);
        dialogPair.first.show();
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
        if (restartDialogMessage == null) {
            restartDialogMessage = str("revanced_settings_restart_dialog_message");
        }
        if (restartDialogButtonText == null) {
            restartDialogButtonText = str("revanced_settings_restart");
        }

        Pair<Dialog, LinearLayout> dialogPair = createCustomDialog(context,
                restartDialogTitle,
                restartDialogMessage,
                restartDialogButtonText,
                () -> Utils.restartApp(context),
                () -> {}, // Cancel action just dismisses the dialog
                null, // No neutral button for this dialog
                null
        );

        dialogPair.first.show();
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
