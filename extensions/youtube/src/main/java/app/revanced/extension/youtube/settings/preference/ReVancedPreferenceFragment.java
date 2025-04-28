package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Insets;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.EnumSetting;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.settings.Settings;

/**
 * Preference fragment for ReVanced settings.
 *
 * @noinspection deprecation
 */
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getBackButtonDrawable() {
        final int backButtonResource = getResourceIdentifier(ThemeHelper.isDarkTheme()
                        ? "yt_outline_arrow_left_white_24"
                        : "yt_outline_arrow_left_black_24",
                "drawable");
        return Utils.getContext().getResources().getDrawable(backButtonResource);
    }

    /**
     * Sorts a preference list by menu entries, but preserves the first value as the first entry.
     *
     * @noinspection SameParameterValue
     */
    private static void sortListPreferenceByValues(ListPreference listPreference, int firstEntriesToPreserve) {
        CharSequence[] entries = listPreference.getEntries();
        CharSequence[] entryValues = listPreference.getEntryValues();
        final int entrySize = entries.length;

        if (entrySize != entryValues.length) {
            // Xml array declaration has a missing/extra entry.
            throw new IllegalStateException();
        }

        List<Pair<String, String>> firstPairs = new ArrayList<>(firstEntriesToPreserve);
        List<Pair<String, String>> pairsToSort = new ArrayList<>(entrySize);

        for (int i = 0; i < entrySize; i++) {
            Pair<String, String> pair = new Pair<>(entries[i].toString(), entryValues[i].toString());
            if (i < firstEntriesToPreserve) {
                firstPairs.add(pair);
            } else {
                pairsToSort.add(pair);
            }
        }

        pairsToSort.sort((pair1, pair2)
                -> pair1.first.compareToIgnoreCase(pair2.first));

        CharSequence[] sortedEntries = new CharSequence[entrySize];
        CharSequence[] sortedEntryValues = new CharSequence[entrySize];

        int i = 0;
        for (Pair<String, String> pair : firstPairs) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        for (Pair<String, String> pair : pairsToSort) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        listPreference.setEntries(sortedEntries);
        listPreference.setEntryValues(sortedEntryValues);
    }

    @Override
    protected void initialize() {
        super.initialize();

        try {
            setPreferenceScreenToolbar(getPreferenceScreen());

            // If the preference was included, then initialize it based on the available playback speed.
            Preference preference = findPreference(Settings.PLAYBACK_SPEED_DEFAULT.key);
            if (preference instanceof ListPreference playbackPreference) {
                CustomPlaybackSpeedPatch.initializeListPreference(playbackPreference);
            }

            sortPreferenceListMenu(Settings.CHANGE_START_PAGE);
            sortPreferenceListMenu(Settings.SPOOF_VIDEO_STREAMS_LANGUAGE);
            sortPreferenceListMenu(BaseSettings.REVANCED_LANGUAGE);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    private void sortPreferenceListMenu(EnumSetting<?> setting) {
        Preference preference = findPreference(setting.key);
        if (preference instanceof ListPreference languagePreference) {
            sortListPreferenceByValues(languagePreference, 1);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fix the system navigation bar color for the root screen.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity() != null ? getActivity().getWindow() : null;
            if (window != null) {
                int navBarColor = ThemeHelper.getBackgroundColor();
                window.setNavigationBarColor(navBarColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    window.setNavigationBarContrastEnforced(true);
                }
            } else {
                Logger.printDebug(() -> "Failed to get Activity window for navigation bar color");
            }
        }
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

                            // Fix the system navigation bar color.
                            Window window = preferenceScreenDialog.getWindow();
                            if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                int navBarColor = ThemeHelper.getBackgroundColor();
                                window.setNavigationBarColor(navBarColor);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                    window.setNavigationBarContrastEnforced(true);
                                }
                            }

                            // Fix edge-to-edge screen with Android 15 and YT 19.45+
                            // https://developer.android.com/develop/ui/views/layout/edge-to-edge#system-bars-insets
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                                    Insets statusInsets = insets.getInsets(WindowInsets.Type.statusBars());
                                    Insets navInsets = insets.getInsets(WindowInsets.Type.navigationBars());
                                    v.setPadding(0, statusInsets.top, 0, navInsets.bottom);
                                    return insets;
                                });
                            }

                            Toolbar toolbar = new Toolbar(childScreen.getContext());
                            toolbar.setTitle(childScreen.getTitle());
                            toolbar.setNavigationIcon(getBackButtonDrawable());
                            toolbar.setNavigationOnClickListener(view -> preferenceScreenDialog.dismiss());
                            final int margin = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
                            );
                            toolbar.setTitleMargin(margin, 0, margin, 0);

                            TextView toolbarTextView = Utils.getChildView(toolbar,
                                    true, TextView.class::isInstance);
                            if (toolbarTextView != null) {
                                toolbarTextView.setTextColor(ThemeHelper.getForegroundColor());
                            }

                            LicenseActivityHook.setToolbarLayoutParams(toolbar);

                            rootView.addView(toolbar, 0);
                            return false;
                        }
                );
            }
        }
    }
}
