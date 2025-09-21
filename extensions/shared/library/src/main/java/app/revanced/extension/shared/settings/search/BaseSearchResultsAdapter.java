package app.revanced.extension.shared.settings.search;

import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;
import static app.revanced.extension.shared.settings.search.BaseSearchViewController.DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.shared.settings.preference.UrlLinkPreference;
import app.revanced.extension.shared.ui.ColorDot;

/**
 * Abstract adapter for displaying search results in overlay ListView with ViewHolder pattern.
 */
@SuppressWarnings("deprecation")
public abstract class BaseSearchResultsAdapter extends ArrayAdapter<BaseSearchResultItem> {
    protected final LayoutInflater inflater;
    protected final BaseSearchViewController.BasePreferenceFragment fragment;
    protected final BaseSearchViewController searchViewController;
    protected AnimatorSet currentAnimator;
    protected abstract PreferenceScreen getMainPreferenceScreen();

    protected static final int BLINK_DURATION = 400;
    protected static final int PAUSE_BETWEEN_BLINKS = 100;

    protected static final int ID_PREFERENCE_TITLE = getResourceIdentifierOrThrow(
            "preference_title", "id");
    protected static final int ID_PREFERENCE_SUMMARY = getResourceIdentifierOrThrow(
            "preference_summary", "id");
    protected static final int ID_PREFERENCE_PATH = getResourceIdentifierOrThrow(
            "preference_path", "id");
    protected static final int ID_PREFERENCE_SWITCH = getResourceIdentifierOrThrow(
            "preference_switch", "id");
    protected static final int ID_PREFERENCE_COLOR_DOT = getResourceIdentifierOrThrow(
            "preference_color_dot", "id");

    protected static class RegularViewHolder {
        TextView titleView;
        TextView summaryView;
    }

    protected static class SwitchViewHolder {
        TextView titleView;
        TextView summaryView;
        Switch switchWidget;
    }

    protected static class ColorViewHolder {
        TextView titleView;
        TextView summaryView;
        View colorDot;
    }

    protected static class GroupHeaderViewHolder {
        TextView pathView;
    }

    protected static class NoResultsViewHolder {
        TextView titleView;
        TextView summaryView;
        ImageView iconView;
    }

    public BaseSearchResultsAdapter(Context context, List<BaseSearchResultItem> items,
                                    BaseSearchViewController.BasePreferenceFragment fragment,
                                    BaseSearchViewController searchViewController) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
        this.fragment = fragment;
        this.searchViewController = searchViewController;
    }

    @Override
    public int getItemViewType(int position) {
        BaseSearchResultItem item = getItem(position);
        return item == null ? 0 : item.preferenceType.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return BaseSearchResultItem.ViewType.values().length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BaseSearchResultItem item = getItem(position);
        if (item == null) return new View(getContext());
        // Use the ViewType enum.
        BaseSearchResultItem.ViewType viewType = item.preferenceType;
        // Create or reuse preference view based on type.
        return createPreferenceView(item, convertView, viewType, parent);
    }

    @Override
    public boolean isEnabled(int position) {
        BaseSearchResultItem item = getItem(position);
        // Disable for NO_RESULTS items to prevent ripple/selection.
        return item != null && item.preferenceType != BaseSearchResultItem.ViewType.NO_RESULTS;
    }

    /**
     * Creates or reuses a view for the given SearchResultItem.
     * <p>
     * Thanks to {@link #getItemViewType(int)} and {@link #getViewTypeCount()}, ListView knows
     * how many different row types exist and keeps a separate "recycling pool" for each.
     * That means convertView passed here is ALWAYS of the correct type for this position.
     * So only need to check if (view == null), and if so – inflate a new layout and create the proper ViewHolder.
     */
    protected View createPreferenceView(BaseSearchResultItem item, View convertView,
                                        BaseSearchResultItem.ViewType viewType, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflateViewForType(viewType, parent);
            createViewHolderForType(view, viewType);
        }

        // Retrieve the cached ViewHolder.
        Object holder = view.getTag();
        bindDataToViewHolder(item, holder, viewType, view);
        return view;
    }

    protected View inflateViewForType(BaseSearchResultItem.ViewType viewType, ViewGroup parent) {
        return inflater.inflate(viewType.getLayoutResourceId(), parent, false);
    }

    protected void createViewHolderForType(View view, BaseSearchResultItem.ViewType viewType) {
        switch (viewType) {
            case REGULAR, LIST, URL_LINK -> {
                RegularViewHolder regularHolder = new RegularViewHolder();
                regularHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                regularHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                view.setTag(regularHolder);
            }
            case SWITCH -> {
                SwitchViewHolder switchHolder = new SwitchViewHolder();
                switchHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                switchHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                switchHolder.switchWidget = view.findViewById(ID_PREFERENCE_SWITCH);
                view.setTag(switchHolder);
            }
            case COLOR_PICKER -> {
                ColorViewHolder colorHolder = new ColorViewHolder();
                colorHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                colorHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                colorHolder.colorDot = view.findViewById(ID_PREFERENCE_COLOR_DOT);
                view.setTag(colorHolder);
            }
            case GROUP_HEADER -> {
                GroupHeaderViewHolder groupHolder = new GroupHeaderViewHolder();
                groupHolder.pathView = view.findViewById(ID_PREFERENCE_PATH);
                view.setTag(groupHolder);
            }
            case NO_RESULTS -> {
                NoResultsViewHolder noResultsHolder = new NoResultsViewHolder();
                noResultsHolder.titleView = view.findViewById(ID_PREFERENCE_TITLE);
                noResultsHolder.summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
                noResultsHolder.iconView = view.findViewById(android.R.id.icon);
                view.setTag(noResultsHolder);
            }
            default -> throw new IllegalStateException("Unknown viewType: " + viewType);
        }
    }

    protected void bindDataToViewHolder(BaseSearchResultItem item, Object holder,
                                        BaseSearchResultItem.ViewType viewType, View view) {
        switch (viewType) {
            case REGULAR, URL_LINK, LIST -> bindRegularViewHolder(item, (RegularViewHolder) holder, view);
            case SWITCH -> bindSwitchViewHolder(item, (SwitchViewHolder) holder, view);
            case COLOR_PICKER -> bindColorViewHolder(item, (ColorViewHolder) holder, view);
            case GROUP_HEADER -> bindGroupHeaderViewHolder(item, (GroupHeaderViewHolder) holder, view);
            case NO_RESULTS -> bindNoResultsViewHolder(item, (NoResultsViewHolder) holder);
            default -> throw new IllegalStateException("Unknown viewType: " + viewType);
        }
    }

    protected void bindRegularViewHolder(BaseSearchResultItem item, RegularViewHolder holder, View view) {
        BaseSearchResultItem.PreferenceSearchItem prefItem = (BaseSearchResultItem.PreferenceSearchItem) item;
        prefItem.refreshHighlighting();
        holder.titleView.setText(item.highlightedTitle);
        holder.summaryView.setText(item.highlightedSummary);
        holder.summaryView.setVisibility(TextUtils.isEmpty(item.highlightedSummary) ? View.GONE : View.VISIBLE);
        setupPreferenceView(view, holder.titleView, holder.summaryView, prefItem.preference,
                () -> {
                    handlePreferenceClick(prefItem.preference);
                    if (prefItem.preference instanceof ListPreference) {
                        prefItem.refreshHighlighting();
                        holder.summaryView.setText(prefItem.getCurrentEffectiveSummary());
                        holder.summaryView.setVisibility(TextUtils.isEmpty(prefItem.highlightedSummary) ? View.GONE : View.VISIBLE);
                        notifyDataSetChanged();
                    }
                },
                () -> navigateAndScrollToPreference(item));
    }

    protected void bindSwitchViewHolder(BaseSearchResultItem item, SwitchViewHolder holder, View view) {
        BaseSearchResultItem.PreferenceSearchItem prefItem = (BaseSearchResultItem.PreferenceSearchItem) item;
        SwitchPreference switchPref = (SwitchPreference) prefItem.preference;
        holder.titleView.setText(item.highlightedTitle);
        holder.switchWidget.setBackground(null); // Remove ripple/highlight.
        // Sync switch state with preference without animation.
        boolean currentState = switchPref.isChecked();
        if (holder.switchWidget.isChecked() != currentState) {
            holder.switchWidget.setChecked(currentState);
            holder.switchWidget.jumpDrawablesToCurrentState();
        }
        prefItem.refreshHighlighting();
        holder.summaryView.setText(prefItem.highlightedSummary);
        holder.summaryView.setVisibility(TextUtils.isEmpty(prefItem.highlightedSummary) ? View.GONE : View.VISIBLE);
        setupPreferenceView(view, holder.titleView, holder.summaryView, switchPref,
                () -> {
                    boolean newState = !switchPref.isChecked();
                    switchPref.setChecked(newState);
                    holder.switchWidget.setChecked(newState);
                    prefItem.refreshHighlighting();
                    holder.summaryView.setText(prefItem.getCurrentEffectiveSummary());
                    holder.summaryView.setVisibility(TextUtils.isEmpty(prefItem.highlightedSummary) ? View.GONE : View.VISIBLE);
                    if (switchPref.getOnPreferenceChangeListener() != null) {
                        switchPref.getOnPreferenceChangeListener().onPreferenceChange(switchPref, newState);
                    }
                    notifyDataSetChanged();
                },
                () -> navigateAndScrollToPreference(item));
        holder.switchWidget.setEnabled(switchPref.isEnabled());
    }

    protected void bindColorViewHolder(BaseSearchResultItem item, ColorViewHolder holder, View view) {
        BaseSearchResultItem.PreferenceSearchItem prefItem = (BaseSearchResultItem.PreferenceSearchItem) item;
        holder.titleView.setText(item.highlightedTitle);
        holder.summaryView.setText(item.highlightedSummary);
        holder.summaryView.setVisibility(TextUtils.isEmpty(item.highlightedSummary) ? View.GONE : View.VISIBLE);
        ColorDot.applyColorDot(holder.colorDot, prefItem.getColor(), prefItem.preference.isEnabled());
        setupPreferenceView(view, holder.titleView, holder.summaryView, prefItem.preference,
                () -> handlePreferenceClick(prefItem.preference),
                () -> navigateAndScrollToPreference(item));
    }

    protected void bindGroupHeaderViewHolder(BaseSearchResultItem item, GroupHeaderViewHolder holder, View view) {
        holder.pathView.setText(item.highlightedTitle);
        view.setOnClickListener(v -> navigateToTargetScreen(item));
    }

    protected void bindNoResultsViewHolder(BaseSearchResultItem item, NoResultsViewHolder holder) {
        holder.titleView.setText(item.highlightedTitle);
        holder.summaryView.setText(item.highlightedSummary);
        holder.summaryView.setVisibility(TextUtils.isEmpty(item.highlightedSummary) ? View.GONE : View.VISIBLE);
        holder.iconView.setImageResource(DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON);
    }

    /**
     * Sets up a preference view with click listeners and proper enabled state handling.
     */
    protected void setupPreferenceView(View view, TextView titleView, TextView summaryView, Preference preference,
                                       Runnable onClickAction, Runnable onLongClickAction) {
        boolean enabled = preference.isEnabled();

        // To enable long-click navigation for disabled settings, manually control the enabled state of the title
        // and summary and disable the ripple effect instead of using 'view.setEnabled(enabled)'.

        titleView.setEnabled(enabled);
        summaryView.setEnabled(enabled);

        if (!enabled) view.setBackground(null); // Disable ripple effect.

        // In light mode, alpha 0.5 is applied to a disabled title automatically,
        // but in dark mode it needs to be applied manually.
        if (Utils.isDarkModeEnabled()) {
            titleView.setAlpha(enabled ? 1.0f : ColorPickerPreference.DISABLED_ALPHA);
        }
        // Set up click and long-click listeners.
        view.setOnClickListener(enabled ? v -> onClickAction.run() : null);
        view.setOnLongClickListener(v -> {
            onLongClickAction.run();
            return true;
        });
    }

    /**
     * Navigates to the settings screen containing the given search result item and triggers scrolling.
     */
    protected void navigateAndScrollToPreference(BaseSearchResultItem item) {
        // No navigation for URL_LINK items.
        if (item.preferenceType == BaseSearchResultItem.ViewType.URL_LINK) return;

        PreferenceScreen targetScreen = navigateToTargetScreen(item);
        if (targetScreen == null) return;
        if (!(item instanceof BaseSearchResultItem.PreferenceSearchItem prefItem)) return;

        Preference targetPreference = prefItem.preference;

        fragment.getView().post(() -> {
            ListView listView = targetScreen == getMainPreferenceScreen()
                    ? getPreferenceListView()
                    : targetScreen.getDialog().findViewById(android.R.id.list);

            if (listView == null) return;

            int targetPosition = findPreferencePosition(targetPreference, listView);
            if (targetPosition == -1) return;

            int firstVisible = listView.getFirstVisiblePosition();
            int lastVisible = listView.getLastVisiblePosition();

            if (targetPosition >= firstVisible && targetPosition <= lastVisible) {
                // The preference is already visible, but still scroll it to the bottom of the list for consistency.
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
        });
    }

    /**
     * Navigates to the final PreferenceScreen using preference keys or titles as fallback.
     */
    protected PreferenceScreen navigateToTargetScreen(BaseSearchResultItem item) {
        PreferenceScreen currentScreen = getMainPreferenceScreen();
        Preference targetPref = null;

        // Try key-based navigation first.
        if (item.navigationKeys != null && !item.navigationKeys.isEmpty()) {
            String finalKey = item.navigationKeys.get(item.navigationKeys.size() - 1);
            targetPref = findPreferenceByKey(currentScreen, finalKey);
        }

        // Fallback to title-based navigation.
        if (targetPref == null && !TextUtils.isEmpty(item.navigationPath)) {
            String[] pathSegments = item.navigationPath.split(" > ");
            String finalSegment = pathSegments[pathSegments.length - 1].trim();
            if (!TextUtils.isEmpty(finalSegment)) {
                targetPref = findPreferenceByTitle(currentScreen, finalSegment);
            }
        }

        if (targetPref instanceof PreferenceScreen targetScreen) {
            handlePreferenceClick(targetScreen);
            return targetScreen;
        }

        return currentScreen;
    }

    /**
     * Recursively searches for a preference by title in a preference group.
     */
    protected Preference findPreferenceByTitle(PreferenceGroup group, String title) {
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference pref = group.getPreference(i);
            CharSequence prefTitle = pref.getTitle();
            if (prefTitle != null && (prefTitle.toString().trim().equalsIgnoreCase(title)
                    || normalizeString(prefTitle.toString()).equals(normalizeString(title)))) {
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
    protected String normalizeString(String input) {
        if (TextUtils.isEmpty(input)) return "";
        return input.trim().toLowerCase().replaceAll("\\s+", " ").replaceAll("[^\\w\\s]", "");
    }

    /**
     * Gets the ListView from the PreferenceFragment.
     */
    protected ListView getPreferenceListView() {
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
    protected ListView findListViewInViewGroup(View view) {
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
    protected int findPreferencePosition(Preference targetPreference, ListView listView) {
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
    protected void highlightPreferenceAtPosition(ListView listView, int position) {
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
    protected void blinkView(View view) {
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
    protected Preference findPreferenceByKey(PreferenceGroup group, String key) {
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
            if (preference instanceof CustomDialogListPreference listPref) {
                BaseSearchResultItem.PreferenceSearchItem searchItem =
                        searchViewController.findSearchItemByPreference(preference);
                if (searchItem != null && searchItem.isEntriesHighlightingApplied()) {
                    listPref.setHighlightedEntriesForDialog(searchItem.getHighlightedEntries());
                }
            }

            Method m = Preference.class.getDeclaredMethod("performClick", PreferenceScreen.class);
            m.setAccessible(true);
            m.invoke(preference, fragment.getPreferenceScreenForSearch());
        } catch (Exception e) {
            Logger.printException(() -> "Failed to invoke performClick()", e);
        }
    }

    /**
     * Checks if a preference has navigation capability (can open a new screen).
     */
    boolean hasNavigationCapability(Preference preference) {
        // PreferenceScreen always allows navigation.
        if (preference instanceof PreferenceScreen) return true;
        // UrlLinkPreference does not navigate to a new screen, it opens an external URL.
        if (preference instanceof UrlLinkPreference) return false;
        // Other group types that might have their own screens.
        if (preference instanceof PreferenceGroup) {
            // Check if it has its own fragment or intent.
            return preference.getIntent() != null || preference.getFragment() != null;
        }
        return false;
    }
}
