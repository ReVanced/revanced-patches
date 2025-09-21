package app.revanced.extension.youtube.settings.search;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Toolbar;

import app.revanced.extension.shared.settings.search.BaseSearchResultItem;
import app.revanced.extension.shared.settings.search.BaseSearchResultsAdapter;
import app.revanced.extension.shared.settings.search.BaseSearchViewController;
import app.revanced.extension.youtube.settings.preference.YouTubePreferenceFragment;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

/**
 * YouTube-specific search view controller implementation.
 */
@SuppressWarnings("deprecation")
public class YouTubeSearchViewController extends BaseSearchViewController {

    public static YouTubeSearchViewController addSearchViewComponents(Activity activity, Toolbar toolbar,
                                                                      YouTubePreferenceFragment fragment) {
        return new YouTubeSearchViewController(activity, toolbar, fragment);
    }

    private YouTubeSearchViewController(Activity activity, Toolbar toolbar, YouTubePreferenceFragment fragment) {
        super(activity, toolbar, new PreferenceFragmentAdapter(fragment));
    }

    @Override
    protected BaseSearchResultsAdapter createSearchResultsAdapter() {
        return new YouTubeSearchResultsAdapter(activity, filteredSearchItems, fragment, this);
    }

    @Override
    protected boolean isSpecialPreferenceGroup(Preference preference) {
        return preference instanceof SponsorBlockPreferenceGroup;
    }

    @Override
    protected void setupSpecialPreferenceListeners(BaseSearchResultItem item) {
    }

    // Static method for Activity finish.
    public static boolean handleFinish(YouTubeSearchViewController searchViewController) {
        if (searchViewController != null && searchViewController.isSearchActive()) {
            searchViewController.closeSearch();
            return true;
        }
        return false;
    }

    // Adapter to wrap YouTubePreferenceFragment to BasePreferenceFragment interface.
    private record PreferenceFragmentAdapter(YouTubePreferenceFragment fragment) implements BasePreferenceFragment {
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
