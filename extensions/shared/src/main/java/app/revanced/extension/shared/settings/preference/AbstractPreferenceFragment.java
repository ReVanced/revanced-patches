package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Insets;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.youtube.ThemeHelper;

@SuppressWarnings("deprecation")
public abstract class AbstractPreferenceFragment extends PreferenceFragment {
    /**
     * Indicates that if a preference changes,
     * to apply the change from the Setting to the UI component.
     */
    public static boolean settingImportInProgress;

    /**
     * Confirm and restart dialog button text and title.
     * Set by subclasses if Strings cannot be added as a resource.
     */
    @Nullable
    protected static String restartDialogButtonText, restartDialogTitle, confirmDialogTitle;

    /**
     * Used to prevent showing reboot dialog, if user cancels a setting user dialog.
     */
    private boolean showingUserDialogMessage;

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            Setting<?> setting = Setting.getSettingFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            if (pref == null) {
                return;
            }
            Logger.printDebug(() -> "Preference changed: " + setting.key);

            // Apply 'Setting <- Preference', unless during importing when it needs to be 'Setting -> Preference'.
            updatePreference(pref, setting, true, settingImportInProgress);
            // Update any other preference availability that may now be different.
            updateUIAvailability();

            if (settingImportInProgress) {
                return;
            }

            if (!showingUserDialogMessage) {
                if (setting.userDialogMessage != null && ((SwitchPreference) pref).isChecked() != (Boolean) setting.defaultValue) {
                    showSettingUserDialogConfirmation((SwitchPreference) pref, (BooleanSetting) setting);
                } else if (setting.rebootApp) {
                    showRestartDialog(getContext());
                }
            }

        } catch (Exception ex) {
            Logger.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getBackButtonDrawable() {
        final int backButtonResource = getResourceIdentifier(ThemeHelper.isDarkTheme()
                        ? "yt_outline_arrow_left_white_24"
                        : "yt_outline_arrow_left_black_24",
                "drawable");
        return Utils.getContext().getResources().getDrawable(backButtonResource);
    }

    /**
     * Initialize this instance, and do any custom behavior.
     * <p>
     * To ensure all {@link Setting} instances are correctly synced to the UI,
     * it is important that subclasses make a call or otherwise reference their Settings class bundle
     * so all app specific {@link Setting} instances are loaded before this method returns.
     */
    protected void initialize() {
        final var identifier = Utils.getResourceIdentifier("revanced_prefs", "xml");
        if (identifier == 0) return;
        addPreferencesFromResource(identifier);

        PreferenceScreen screen = getPreferenceScreen();
        Utils.sortPreferenceGroups(screen);
        Utils.setPreferenceTitlesToMultiLineIfNeeded(screen);
    }

    private void showSettingUserDialogConfirmation(SwitchPreference switchPref, BooleanSetting setting) {
        Utils.verifyOnMainThread();

        final var context = getContext();
        if (confirmDialogTitle == null) {
            confirmDialogTitle = str("revanced_settings_confirm_user_dialog_title");
        }
        showingUserDialogMessage = true;
        new AlertDialog.Builder(context)
                .setTitle(confirmDialogTitle)
                .setMessage(Objects.requireNonNull(setting.userDialogMessage).toString())
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (setting.rebootApp) {
                        showRestartDialog(context);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    switchPref.setChecked(setting.defaultValue); // Recursive call that resets the Setting value.
                })
                .setOnDismissListener(dialog -> {
                    showingUserDialogMessage = false;
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Updates all Preferences values and their availability using the current values in {@link Setting}.
     */
    protected void updateUIToSettingValues() {
        updatePreferenceScreen(getPreferenceScreen(), true,true);
    }

    /**
     * Updates Preferences availability only using the status of {@link Setting}.
     */
    protected void updateUIAvailability() {
        updatePreferenceScreen(getPreferenceScreen(), false, false);
    }

    /**
     * Syncs all UI Preferences to any {@link Setting} they represent.
     */
    private void updatePreferenceScreen(@NonNull PreferenceScreen screen,
                                        boolean syncSettingValue,
                                        boolean applySettingToPreference) {
        // Alternatively this could iterate thru all Settings and check for any matching Preferences,
        // but there are many more Settings than UI preferences so it's more efficient to only check
        // the Preferences.
        for (int i = 0, prefCount = screen.getPreferenceCount(); i < prefCount; i++) {
            Preference pref = screen.getPreference(i);
            if (pref instanceof PreferenceScreen) {
                updatePreferenceScreen((PreferenceScreen) pref, syncSettingValue, applySettingToPreference);
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
        if (pref instanceof SwitchPreference) {
            SwitchPreference switchPref = (SwitchPreference) pref;
            BooleanSetting boolSetting = (BooleanSetting) setting;
            if (applySettingToPreference) {
                switchPref.setChecked(boolSetting.get());
            } else {
                BooleanSetting.privateSetValue(boolSetting, switchPref.isChecked());
            }
        } else if (pref instanceof EditTextPreference) {
            EditTextPreference editPreference = (EditTextPreference) pref;
            if (applySettingToPreference) {
                editPreference.setText(setting.get().toString());
            } else {
                Setting.privateSetValueFromString(setting, editPreference.getText());
            }
        } else if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
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

    public static void showRestartDialog(@NonNull final Context context) {
        Utils.verifyOnMainThread();
        if (restartDialogTitle == null) {
            restartDialogTitle = str("revanced_settings_restart_title");
        }
        if (restartDialogButtonText == null) {
            restartDialogButtonText = str("revanced_settings_restart");
        }
        new AlertDialog.Builder(context)
                .setMessage(restartDialogTitle)
                .setPositiveButton(restartDialogButtonText, (dialog, id)
                        -> Utils.restartApp(context))
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .show();
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
            setPreferenceScreenToolbar(getPreferenceScreen());

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

    private void setPreferenceScreenToolbar(PreferenceScreen parentScreen) {
        for (int i = 0, preferenceCount = parentScreen.getPreferenceCount(); i < preferenceCount; i++) {
            Preference childPreference = parentScreen.getPreference(i);
            if (childPreference instanceof PreferenceScreen) {
                // Recursively set sub preferences.
                setPreferenceScreenToolbar((PreferenceScreen) childPreference);

                childPreference.setOnPreferenceClickListener(
                        childScreen -> {
                            Dialog preferenceScreenDialog = ((PreferenceScreen) childScreen).getDialog();
                            ViewGroup rootView = (ViewGroup) preferenceScreenDialog
                                    .findViewById(android.R.id.content)
                                    .getParent();

                            // Fix required for Android 15 and YT 19.45+
                            // FIXME:
                            // On Android 15 the text layout is not aligned the same as the parent
                            // screen and it looks a little off.  Otherwise this works.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                                    Insets statusInsets = insets.getInsets(WindowInsets.Type.statusBars());
                                    v.setPadding(0, statusInsets.top, 0, 0);
                                    return insets;
                                });
                            }

                            Toolbar toolbar = new Toolbar(childScreen.getContext());
                            toolbar.setTitle(childScreen.getTitle());
                            toolbar.setNavigationIcon(getBackButtonDrawable());
                            toolbar.setNavigationOnClickListener(view -> preferenceScreenDialog.dismiss());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                final int margin = (int) TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
                                );
                                toolbar.setTitleMargin(margin, 0, margin, 0);
                            }

                            TextView toolbarTextView = Utils.getChildView(toolbar,
                                    true, TextView.class::isInstance);
                            if (toolbarTextView != null) {
                                toolbarTextView.setTextColor(ThemeHelper.getForegroundColor());
                            }

                            rootView.addView(toolbar, 0);
                            return false;
                        }
                );
            }
        }
    }
}
