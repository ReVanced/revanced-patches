package app.revanced.extension.youtube.settings.search;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;
import static app.revanced.extension.youtube.settings.search.SearchViewController.DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON;

import android.animation.*;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.ui.ColorDot;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying search results in overlay ListView with ViewHolder pattern.
 */
@SuppressWarnings("deprecation")
public class SearchResultsAdapter extends ArrayAdapter<SearchResultItem> {
    private final LayoutInflater inflater;
    private final ReVancedPreferenceFragment fragment;
    private AnimatorSet currentAnimator;

    private static final int BLINK_DURATION = 400;
    private static final int PAUSE_BETWEEN_BLINKS = 100;

    // Resource ID constants.
    private static final int ID_PREFERENCE_TITLE = getResourceIdentifier("preference_title", "id");
    private static final int ID_PREFERENCE_SUMMARY = getResourceIdentifier("preference_summary", "id");
    private static final int ID_PREFERENCE_PATH = getResourceIdentifier("preference_path", "id");
    private static final int ID_PREFERENCE_SWITCH = getResourceIdentifier("preference_switch", "id");
    private static final int ID_PREFERENCE_COLOR_DOT = getResourceIdentifier("preference_color_dot", "id");

    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_REGULAR =
            getResourceIdentifier("revanced_preference_search_result_regular", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_SWITCH =
            getResourceIdentifier("revanced_preference_search_result_switch", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_LIST =
            getResourceIdentifier("revanced_preference_search_result_list", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_COLOR =
            getResourceIdentifier("revanced_preference_search_result_color", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_GROUP_HEADER =
            getResourceIdentifier("revanced_preference_search_result_group_header", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_NO_RESULT =
            getResourceIdentifier("revanced_preference_search_no_result", "layout");

    // Layout resource mapping.
    private static final Map<String, String> LAYOUT_RESOURCE_MAP = Map.of(
            "regular", "revanced_preference_search_result_regular",
            "switch", "revanced_preference_search_result_switch",
            "list", "revanced_preference_search_result_list",
            "color", "revanced_preference_search_result_color",
            "segment_category", "revanced_preference_search_result_color",
            "group_header", "revanced_preference_search_result_group_header",
            "no_results", "revanced_preference_search_no_result");

    // ViewHolder for regular and list preferences.
    private static class RegularViewHolder {
        TextView titleView;
        TextView summaryView;
    }

    // ViewHolder for switch preferences.
    private static class SwitchViewHolder {
        TextView titleView;
        TextView summaryView;
        Switch switchWidget;
    }

    // ViewHolder for color preferences.
    private static class ColorViewHolder {
        TextView titleView;
        TextView summaryView;
        View colorDot;
    }

    // ViewHolder for group header.
    private static class GroupHeaderViewHolder {
        TextView pathView;
    }

    // ViewHolder for no results preferences.
    private static class NoResultsViewHolder {
        TextView titleView;
        TextView summaryView;
        ImageView iconView;
    }

    public SearchResultsAdapter(Context context, List<SearchResultItem> items, ReVancedPreferenceFragment fragment) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SearchResultItem item = getItem(position);
        if (item == null) return new View(getContext());

        // Map preference type to view type string.
        String viewType = switch (item.preferenceType) {
            case SearchResultItem.TYPE_SWITCH -> "switch";
            case SearchResultItem.TYPE_LIST   -> "list";
            case SearchResultItem.TYPE_COLOR_PICKER -> "color";
            case SearchResultItem.TYPE_SEGMENT_CATEGORY -> "segment_category";
            case SearchResultItem.TYPE_GROUP_HEADER -> "group_header";
            case SearchResultItem.TYPE_NO_RESULTS   -> "no_results";
            default -> "regular";
        };

        // Create or reuse preference view based on type.
        View view = createPreferenceView(item, convertView, viewType, parent);

        // Add long-click listener for preference items.
        if (item.preferenceType != SearchResultItem.TYPE_NO_RESULTS
                && item.preferenceType != SearchResultItem.TYPE_GROUP_HEADER) {
            view.setOnLongClickListener(v -> {
                if (LicenseActivityHook.searchViewController != null) {
                    LicenseActivityHook.searchViewController.closeSearch();
                }
                navigateToPreferenceScreen(item);
                return true;
            });
        }

        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        SearchResultItem item = getItem(position);
        // Disable for "no_results" items to prevent ripple/selection.
        return item != null && item.preferenceType != SearchResultItem.TYPE_NO_RESULTS;
    }

    /**
     * Creates a view for a preference or header based on its type using ViewHolder pattern.
     */
    @SuppressWarnings("deprecation")
    private View createPreferenceView(SearchResultItem item, View convertView, String viewType, ViewGroup parent) {
        View view = convertView;
        String layoutResource = LAYOUT_RESOURCE_MAP.get(viewType);
        if (layoutResource == null) {
            Logger.printException(() -> "Invalid viewType: " + viewType);
            return new View(getContext()); // Fallback to empty view.
        }

        int layoutId = switch (layoutResource) {
            case "revanced_preference_search_result_regular" -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_REGULAR;
            case "revanced_preference_search_result_switch"  -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_SWITCH;
            case "revanced_preference_search_result_list"    -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_LIST;
            case "revanced_preference_search_result_color"   -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_COLOR;
            case "revanced_preference_search_result_group_header" -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_GROUP_HEADER;
            case "revanced_preference_search_no_result"      -> LAYOUT_REVANCED_PREFERENCE_SEARCH_NO_RESULT;
            default -> throw new IllegalStateException("Unknown layout resource: " + layoutResource);
        };

        Object holder;
        if (view == null || !viewType.equals(view.getTag())) {
            view = inflater.inflate(layoutId, parent, false);
            view.setTag(viewType);

            // Initialize ViewHolder based on view type.
            switch (viewType) {
                case "regular", "list" -> {
                    RegularViewHolder regularHolder = new RegularViewHolder();
                    regularHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                    regularHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                    view.setTag(ID_PREFERENCE_TITLE, regularHolder);
                    holder = regularHolder;
                }
                case "switch" -> {
                    SwitchViewHolder switchHolder = new SwitchViewHolder();
                    switchHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                    switchHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                    switchHolder.switchWidget = view.findViewById(ID_PREFERENCE_SWITCH);
                    view.setTag(ID_PREFERENCE_TITLE, switchHolder);
                    holder = switchHolder;
                }
                case "color", "segment_category" -> {
                    ColorViewHolder colorHolder = new ColorViewHolder();
                    colorHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                    colorHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                    colorHolder.colorDot = view.findViewById(ID_PREFERENCE_COLOR_DOT);
                    view.setTag(ID_PREFERENCE_TITLE, colorHolder);
                    holder = colorHolder;
                }
                case "group_header" -> {
                    GroupHeaderViewHolder groupHolder = new GroupHeaderViewHolder();
                    groupHolder.pathView = view.findViewById(ID_PREFERENCE_PATH);
                    view.setTag(ID_PREFERENCE_TITLE, groupHolder);
                    holder = groupHolder;
                }
                case "no_results" -> {
                    NoResultsViewHolder noResultsHolder = new NoResultsViewHolder();
                    noResultsHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                    noResultsHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                    noResultsHolder.iconView = view.findViewById(android.R.id.icon);
                    view.setTag(ID_PREFERENCE_TITLE, noResultsHolder);
                    holder = noResultsHolder;
                }
                default -> throw new IllegalStateException("Unknown viewType: " + viewType);
            }
        } else {
            holder = view.getTag(ID_PREFERENCE_TITLE);
        }

        // Bind data to ViewHolder.
        switch (viewType) {
            case "regular", "list" -> {
                RegularViewHolder regularHolder = (RegularViewHolder) holder;
                regularHolder.titleView.setText(item.title);
                regularHolder.summaryView.setText(item.summary);
                regularHolder.summaryView.setVisibility(TextUtils.isEmpty(item.summary) ? View.GONE : View.VISIBLE);
                setupPreferenceView(view, regularHolder.titleView, regularHolder.summaryView,
                        ((SearchResultItem.PreferenceSearchItem) item).preference, () ->
                                handlePreferenceClick(((SearchResultItem.PreferenceSearchItem) item).preference));
            }
            case "switch" -> {
                SwitchViewHolder switchHolder = (SwitchViewHolder) holder;
                SearchResultItem.PreferenceSearchItem prefItem = (SearchResultItem.PreferenceSearchItem) item;
                SwitchPreference switchPref = (SwitchPreference) prefItem.preference;
                switchHolder.titleView.setText(item.title);
                switchHolder.switchWidget.setBackground(null); // Remove ripple/highlight.
                // Set switch state without animation.
                boolean currentState = switchPref.isChecked();
                if (switchHolder.switchWidget.isChecked() != currentState) {
                    switchHolder.switchWidget.setChecked(currentState);
                    switchHolder.switchWidget.jumpDrawablesToCurrentState();
                }
                // Update summary based on switch state.
                CharSequence summaryText = currentState
                        ? (switchPref.getSummaryOn() != null ? switchPref.getSummaryOn() :
                        switchPref.getSummary() != null ? switchPref.getSummary() : "")
                        : (switchPref.getSummaryOff() != null ? switchPref.getSummaryOff() :
                        switchPref.getSummary() != null ? switchPref.getSummary() : "");
                switchHolder.summaryView.setText(summaryText);
                switchHolder.summaryView.setVisibility(TextUtils.isEmpty(summaryText) ? View.GONE : View.VISIBLE);
                // Set up click listeners for switch.
                final View finalView = view;
                setupPreferenceView(view, switchHolder.titleView, switchHolder.summaryView,
                        switchPref, () -> {
                            boolean newState = !switchPref.isChecked();
                            switchPref.setChecked(newState);
                            switchHolder.switchWidget.setChecked(newState);
                            // Update summary.
                            CharSequence newSummary = newState
                                    ? (switchPref.getSummaryOn() != null ? switchPref.getSummaryOn() :
                                    switchPref.getSummary() != null ? switchPref.getSummary() : "")
                                    : (switchPref.getSummaryOff() != null ? switchPref.getSummaryOff() :
                                    switchPref.getSummary() != null ? switchPref.getSummary() : "");
                            switchHolder.summaryView.setText(newSummary);
                            switchHolder.summaryView.setVisibility(TextUtils.isEmpty(newSummary) ? View.GONE : View.VISIBLE);
                            // Notify preference change.
                            if (switchPref.getOnPreferenceChangeListener() != null) {
                                switchPref.getOnPreferenceChangeListener().onPreferenceChange(switchPref, newState);
                            }

                            notifyDataSetChanged();
                        });
                switchHolder.switchWidget.setEnabled(switchPref.isEnabled());
                switchHolder.switchWidget.setOnClickListener(switchPref.isEnabled()
                        ? v -> finalView.performClick()
                        : null);
            }
            case "color", "segment_category" -> {
                ColorViewHolder colorHolder = (ColorViewHolder) holder;
                SearchResultItem.PreferenceSearchItem prefItem = (SearchResultItem.PreferenceSearchItem) item;
                colorHolder.titleView.setText(item.title);
                colorHolder.summaryView.setText(item.summary);
                colorHolder.summaryView.setVisibility(TextUtils.isEmpty(item.summary) ? View.GONE : View.VISIBLE);
                ColorDot.applyColorDot(
                        colorHolder.colorDot,
                        prefItem.getColor(),
                        prefItem.preference.isEnabled()
                );
                setupPreferenceView(view, colorHolder.titleView, colorHolder.summaryView,
                        prefItem.preference, () -> handlePreferenceClick(prefItem.preference));
            }
            case "group_header" -> {
                GroupHeaderViewHolder groupHolder = (GroupHeaderViewHolder) holder;
                groupHolder.pathView.setText(item.title);
                view.setOnClickListener(v -> navigateToPreferenceScreen(item));
            }
            case "no_results" -> {
                NoResultsViewHolder noResultsHolder = (NoResultsViewHolder) holder;
                noResultsHolder.titleView.setText(item.title);
                noResultsHolder.summaryView.setText(item.summary);
                noResultsHolder.summaryView.setVisibility(TextUtils.isEmpty(item.summary) ? View.GONE : View.VISIBLE);
                noResultsHolder.iconView.setImageResource(DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON);
            }
        }

        return view;
    }

    /**
     * Sets up common properties for preference views.
     */
    @SuppressWarnings("deprecation")
    private void setupPreferenceView(View view, TextView titleView, TextView summaryView,
                                     Preference preference, Runnable onClickAction) {
        boolean enabled = preference.isEnabled();

        // To enable long-click navigation for disabled settings, manually control the enabled state of the title and summary,
        // and disable the ripple effect instead of using 'view.setEnabled(enabled)'.

        titleView.setEnabled(enabled);
        if (summaryView != null) summaryView.setEnabled(enabled);

        if (!enabled) view.setBackground(null); // Disable ripple effect.

        // In light mode, alpha 0.5 is applied to a disabled title automatically,
        // but in dark mode it needs to be applied manually.
        if (Utils.isDarkModeEnabled()) {
            titleView.setAlpha(enabled ? 1.0f : ColorPickerPreference.DISABLED_ALPHA);
        }

        view.setOnClickListener(enabled ? v -> onClickAction.run() : null);
    }

    /**
     * Navigates to the settings screen containing the given search result item and triggers scrolling.
     */
    private void navigateToPreferenceScreen(SearchResultItem item) {
        // No navigation for "no results" item.
        if (item.preferenceType == SearchResultItem.TYPE_NO_RESULTS) {
            return;
        }

        PreferenceScreen targetScreen = navigateByKeys(item); // Try navigation by keys first.
        if (targetScreen == null) {
            targetScreen = navigateByTitles(item); // Fallback to method using titles.
        }
        if (targetScreen == null) {
            return;
        }

        PreferenceScreen finalTargetScreen = targetScreen;
        fragment.getView().post(() -> {
            if (item instanceof SearchResultItem.PreferenceSearchItem prefItem) {
                scrollToPreferenceInCurrentScreen(prefItem.preference, finalTargetScreen);
            }
        });
    }

    /**
     * Navigates directly to the final PreferenceScreen using preference keys.
     */
    private PreferenceScreen navigateByKeys(SearchResultItem item) {
        PreferenceScreen currentScreen = fragment.getPreferenceScreenForSearch();
        if (item.navigationKeys == null || item.navigationKeys.isEmpty()) {
            return currentScreen;
        }

        PreferenceScreen targetScreen = currentScreen;
        String finalKey = item.navigationKeys.get(item.navigationKeys.size() - 1);
        Preference targetPref = findPreferenceByKey(currentScreen, finalKey);
        if (targetPref == null) {
            return null;
        }
        if (targetPref instanceof PreferenceScreen) {
            targetScreen = (PreferenceScreen) targetPref;
            handlePreferenceClick(targetScreen);
        }

        return targetScreen;
    }

    /**
     * Fallback navigation directly to the final PreferenceScreen by titles.
     */
    private PreferenceScreen navigateByTitles(SearchResultItem item) {
        PreferenceScreen currentScreen = fragment.getPreferenceScreenForSearch();
        String[] pathSegments = item.navigationPath.split(" > ");
        if (pathSegments.length == 0 || (pathSegments.length == 1 && pathSegments[0].trim().isEmpty())) {
            return currentScreen;
        }

        String finalSegment = pathSegments[pathSegments.length - 1].trim();
        if (TextUtils.isEmpty(finalSegment)) {
            return currentScreen;
        }

        PreferenceScreen targetScreen = currentScreen;
        Preference foundPref = null;
        for (int i = 0; i < currentScreen.getPreferenceCount(); i++) {
            Preference pref = currentScreen.getPreference(i);
            CharSequence title = pref.getTitle();
            if (title != null) {
                String prefTitle = title.toString().trim();
                if (prefTitle.equalsIgnoreCase(finalSegment) ||
                        normalizeString(prefTitle).equals(normalizeString(finalSegment))) {
                    foundPref = pref;
                    break;
                }
            }
            if (pref instanceof PreferenceGroup) {
                Preference recursiveFound = findPreferenceByTitle((PreferenceGroup) pref, finalSegment);
                if (recursiveFound != null) {
                    foundPref = recursiveFound;
                    break;
                }
            }
        }

        if (foundPref == null) {
            Logger.printDebug(() -> "Could not find preference group: " + finalSegment + " in path: " + item.navigationPath);
            return null;
        }

        if (foundPref instanceof PreferenceScreen) {
            targetScreen = (PreferenceScreen) foundPref;
            handlePreferenceClick(targetScreen);
        }

        return targetScreen;
    }

    /**
     * Recursively searches for a preference by title in a preference group.
     */
    private Preference findPreferenceByTitle(PreferenceGroup group, String title) {
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference pref = group.getPreference(i);
            CharSequence prefTitle = pref.getTitle();
            if (prefTitle != null && (
                    prefTitle.toString().trim().equalsIgnoreCase(title) ||
                            normalizeString(prefTitle.toString()).equals(normalizeString(title)))) {
                return pref;
            }
            if (pref instanceof PreferenceGroup) {
                Preference found = findPreferenceByTitle((PreferenceGroup) pref, title);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Normalizes string for comparison (removes extra characters, spaces etc).
     */
    private String normalizeString(String input) {
        if (TextUtils.isEmpty(input)) return "";
        return input.trim()
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("[^\\w\\s]", "");
    }

    /**
     * Scrolls to a preference in the target PreferenceScreen and highlights it after scrolling finishes.
     */
    private void scrollToPreferenceInCurrentScreen(Preference targetPreference, PreferenceScreen targetScreen) {
        ListView listView;
        if (targetScreen == fragment.getPreferenceScreenForSearch()) {
            listView = getPreferenceListView();
        } else {
            Dialog dialog = targetScreen.getDialog();
            listView = dialog.findViewById(android.R.id.list);
        }
        if (listView == null) return;

        int targetPosition = findPreferencePosition(targetPreference, listView);
        if (targetPosition == -1) return;

        int firstVisible = listView.getFirstVisiblePosition();
        int lastVisible = listView.getLastVisiblePosition();

        if (targetPosition >= firstVisible && targetPosition <= lastVisible) {
            // The preference is already visible, but still scroll it to the bottom of the list for consistency.
            listView.post(() -> {
                View child = listView.getChildAt(targetPosition - firstVisible);
                if (child != null) {
                    // Calculate how much to scroll so the item is aligned at the bottom.
                    int scrollAmount = child.getBottom() - listView.getHeight();
                    if (scrollAmount > 0) {
                        // Perform smooth scroll animation for better user experience.
                        listView.smoothScrollBy(scrollAmount, 300);
                    }
                }
                // Highlight the preference once it is positioned.
                highlightPreferenceAtPosition(listView, targetPosition);
            });
        } else {
            // The preference is outside of the current visible range, scroll to it from the top.
            listView.smoothScrollToPositionFromTop(targetPosition, 0);

            Handler handler = new Handler(Looper.getMainLooper());
            // Fallback runnable in case the OnScrollListener does not trigger.
            Runnable fallback = () -> {
                listView.setOnScrollListener(null);
                highlightPreferenceAtPosition(listView, targetPosition);
            };
            // Post fallback with a small delay.
            handler.postDelayed(fallback, 350);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                private boolean isScrolling = false;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
                        // Mark that scrolling has started.
                        isScrolling = true;
                    }
                    if (scrollState == SCROLL_STATE_IDLE && isScrolling) {
                        // Scrolling is finished, cleanup listener and cancel fallback.
                        isScrolling = false;
                        listView.setOnScrollListener(null);
                        handler.removeCallbacks(fallback);
                        // Highlight the target preference when scrolling is done.
                        highlightPreferenceAtPosition(listView, targetPosition);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
            });
        }
    }

    /**
     * Gets the ListView from the PreferenceFragment.
     */
    private ListView getPreferenceListView() {
        View fragmentView = fragment.getView();
        if (fragmentView != null) {
            ListView listView = findListViewInViewGroup(fragmentView);
            if (listView != null) {
                return listView;
            }
        }

        return fragment.getActivity().findViewById(android.R.id.list);
    }

    /**
     * Recursively searches for a ListView in a ViewGroup.
     */
    private ListView findListViewInViewGroup(View view) {
        if (view instanceof ListView) {
            return (ListView) view;
        }
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                ListView result = findListViewInViewGroup(group.getChildAt(i));
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Finds the position of a preference in the ListView adapter.
     */
    @SuppressWarnings("deprecation")
    private int findPreferencePosition(Preference targetPreference, ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return -1;
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item == targetPreference) {
                return i;
            }
            if (item instanceof Preference pref && targetPreference.getKey() != null) {
                if (targetPreference.getKey().equals(pref.getKey())) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Highlights a preference at the specified position with a blink effect.
     */
    private void highlightPreferenceAtPosition(ListView listView, int position) {
        int firstVisible = listView.getFirstVisiblePosition();
        if (position < firstVisible || position > listView.getLastVisiblePosition()) {
            return;
        }

        View itemView = listView.getChildAt(position - firstVisible);
        if (itemView != null) {
            blinkView(itemView);
        }
    }

    /**
     * Creates a smooth double-blink effect on a view's background without affecting the text.
     * @param view The View to apply the animation to.
     */
    private void blinkView(View view) {
        // If a previous animation is still running, cancel it to prevent conflicts.
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }
        int startColor = Utils.getAppBackgroundColor();
        int highlightColor = Utils.adjustColorBrightness(
                startColor,
                Utils.isDarkModeEnabled() ? 1.25f : 0.8f
        );
        // Animator for transitioning from the start color to the highlight color.
        ObjectAnimator fadeIn = ObjectAnimator.ofObject(
                view,
                "backgroundColor",
                new ArgbEvaluator(),
                startColor,
                highlightColor
        );
        fadeIn.setDuration(BLINK_DURATION);
        // Animator to return to the start color.
        ObjectAnimator fadeOut = ObjectAnimator.ofObject(
                view,
                "backgroundColor",
                new ArgbEvaluator(),
                highlightColor,
                startColor
        );
        fadeOut.setDuration(BLINK_DURATION);

        currentAnimator = new AnimatorSet();
        // Create the sequence: fadeIn -> fadeOut -> (pause) -> fadeIn -> fadeOut.
        AnimatorSet firstBlink = new AnimatorSet();
        firstBlink.playSequentially(fadeIn, fadeOut);
        AnimatorSet secondBlink = new AnimatorSet();
        secondBlink.playSequentially(fadeIn.clone(), fadeOut.clone()); // Use clones for the second blink.

        currentAnimator.play(secondBlink).after(firstBlink).after(PAUSE_BETWEEN_BLINKS);
        currentAnimator.start();
    }

    /**
     * Recursively finds a preference by key in a preference group.
     */
    @SuppressWarnings("deprecation")
    private Preference findPreferenceByKey(PreferenceGroup group, String key) {
        if (group == null || TextUtils.isEmpty(key)) {
            return null;
        }

        // First search on current level.
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference pref = group.getPreference(i);
            if (key.equals(pref.getKey())) {
                return pref;
            }
            if (pref instanceof PreferenceGroup) {
                Preference found = findPreferenceByKey((PreferenceGroup) pref, key);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * Handles preference click actions by invoking the preference's performClick method via reflection.
     */
    @SuppressWarnings("all")
    private void handlePreferenceClick(Preference preference) {
        try {
            Method m = Preference.class.getDeclaredMethod("performClick", PreferenceScreen.class);
            m.setAccessible(true);
            m.invoke(preference, fragment.getPreferenceScreen());
        } catch (Exception e) {
            Logger.printException(() -> "Failed to invoke performClick()", e);
        }
    }

    /**
     * Checks if a preference has navigation capability (can open a new screen).
     */
    @SuppressWarnings("deprecation")
    boolean hasNavigationCapability(Preference preference) {
        // PreferenceScreen always allows navigation.
        if (preference instanceof PreferenceScreen) {
            return true;
        }
        // Other group types that might have their own screens.
        if (preference instanceof PreferenceGroup) {
            // Check if it has its own fragment or intent.
            return preference.getIntent() != null || preference.getFragment() != null;
        }

        return false;
    }
}
