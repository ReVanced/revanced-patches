package app.revanced.extension.youtube.settings.search;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toolbar;

import app.revanced.extension.shared.settings.search.*;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

/**
 * YouTube-specific search view controller implementation.
 */
@SuppressWarnings("deprecation")
public class YouTubeSearchViewController extends BaseSearchViewController {

    public static YouTubeSearchViewController addSearchViewComponents(Activity activity, Toolbar toolbar,
                                                                      ReVancedPreferenceFragment fragment) {
        return new YouTubeSearchViewController(activity, toolbar, fragment);
    }

    private YouTubeSearchViewController(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        super(activity, toolbar, new PreferenceFragmentAdapter(fragment));
    }

    @Override
    protected BaseSearchResultsAdapter createSearchResultsAdapter() {
        return new YouTubeSearchResultsAdapter(activity, filteredSearchItems, fragment, this);
    }

    @Override
    protected BaseSearchHistoryManager createSearchHistoryManager(FrameLayout overlayContainer,
                                                                  BaseSearchHistoryManager.OnSelectHistoryItemListener listener) {
        return new YouTubeSearchHistoryManager(activity, overlayContainer, listener);
    }

    @Override
    protected boolean isNotSpecialPreferenceGroup(Preference preference) {
        return !(preference instanceof SponsorBlockPreferenceGroup);
    }

    @Override
    protected void setupSpecialPreferenceListeners(BaseSearchResultItem item) {
    }

    // Static method for back press handling.
    public static boolean handleBackPress(YouTubeSearchViewController searchViewController) {
        if (searchViewController != null && searchViewController.isSearchActive()) {
            searchViewController.closeSearch();
            return true;
        }
        return false;
    }

    // Adapter to wrap ReVancedPreferenceFragment to BasePreferenceFragment interface.
    private static class PreferenceFragmentAdapter implements BasePreferenceFragment {
        private final ReVancedPreferenceFragment fragment;

        public PreferenceFragmentAdapter(ReVancedPreferenceFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public PreferenceScreen getPreferenceScreenForSearch() {
            return fragment.getPreferenceScreenForSearch();
        }

        @Override
        public View getView() {
            return fragment.getView();
        }

        @Override
        public Activity getActivity() {
            return fragment.getActivity();
        }
    }
}
