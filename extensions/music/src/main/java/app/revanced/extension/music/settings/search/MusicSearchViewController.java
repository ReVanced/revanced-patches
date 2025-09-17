package app.revanced.extension.music.settings.search;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toolbar;

import app.revanced.extension.music.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.shared.settings.search.*;

/**
 * Music-specific search view controller implementation.
 */
@SuppressWarnings("deprecation")
public class MusicSearchViewController extends BaseSearchViewController {

    public static MusicSearchViewController addSearchViewComponents(Activity activity, Toolbar toolbar,
                                                                    ReVancedPreferenceFragment fragment) {
        return new MusicSearchViewController(activity, toolbar, fragment);
    }

    private MusicSearchViewController(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        super(activity, toolbar, new PreferenceFragmentAdapter(fragment));
    }

    @Override
    protected BaseSearchResultsAdapter createSearchResultsAdapter() {
        return new MusicSearchResultsAdapter(activity, filteredSearchItems, fragment, this);
    }

    @Override
    protected BaseSearchHistoryManager createSearchHistoryManager(FrameLayout overlayContainer,
                                                                  BaseSearchHistoryManager.OnSelectHistoryItemListener listener) {
        return new MusicSearchHistoryManager(activity, overlayContainer, listener);
    }

    @Override
    protected boolean isNotSpecialPreferenceGroup(Preference preference) {
        // Music doesn't have SponsorBlock, so no special groups.
        return true;
    }

    @Override
    protected void setupSpecialPreferenceListeners(BaseSearchResultItem item) {
        // Music doesn't have special preferences.
        // This method can be empty or handle music-specific preferences if any.
    }

    @Override
    protected PreferenceTypeResolver createPreferenceTypeResolver() {
        return new MusicPreferenceTypeResolver();
    }

    // Static method for back press handling.
    public static boolean handleBackPress(MusicSearchViewController searchViewController) {
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
