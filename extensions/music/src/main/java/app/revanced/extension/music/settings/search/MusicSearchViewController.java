package app.revanced.extension.music.settings.search;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Toolbar;

import app.revanced.extension.music.settings.preference.MusicPreferenceFragment;
import app.revanced.extension.shared.settings.search.*;

/**
 * Music-specific search view controller implementation.
 */
@SuppressWarnings("deprecation")
public class MusicSearchViewController extends BaseSearchViewController {

    public static MusicSearchViewController addSearchViewComponents(Activity activity, Toolbar toolbar,
                                                                    MusicPreferenceFragment fragment) {
        return new MusicSearchViewController(activity, toolbar, fragment);
    }

    private MusicSearchViewController(Activity activity, Toolbar toolbar, MusicPreferenceFragment fragment) {
        super(activity, toolbar, new PreferenceFragmentAdapter(fragment));
    }

    @Override
    protected BaseSearchResultsAdapter createSearchResultsAdapter() {
        return new MusicSearchResultsAdapter(activity, filteredSearchItems, fragment, this);
    }

    @Override
    protected boolean isSpecialPreferenceGroup(Preference preference) {
        // Music doesn't have SponsorBlock, so no special groups.
        return false;
    }

    @Override
    protected void setupSpecialPreferenceListeners(BaseSearchResultItem item) {
        // Music doesn't have special preferences.
        // This method can be empty or handle music-specific preferences if any.
    }

    // Static method for handling Activity finish
    public static boolean handleFinish(MusicSearchViewController searchViewController) {
        if (searchViewController != null && searchViewController.isSearchActive()) {
            searchViewController.closeSearch();
            return true;
        }
        return false;
    }

    // Adapter to wrap MusicPreferenceFragment to BasePreferenceFragment interface.
    private record PreferenceFragmentAdapter(MusicPreferenceFragment fragment) implements BasePreferenceFragment {

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
