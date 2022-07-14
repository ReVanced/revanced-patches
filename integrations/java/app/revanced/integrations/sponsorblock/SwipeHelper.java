package app.revanced.integrations.sponsorblock;

import android.content.Context;
import android.content.res.Resources;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import app.revanced.integrations.sponsorblock.player.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

/* loaded from: classes6.dex */
public class SwipeHelper {
    static FrameLayout _frameLayout;
    public static boolean isTabletMode;
    public static ViewGroup nextGenWatchLayout;

    public static void SetFrameLayout(Object obj) {
        try {
            _frameLayout = (FrameLayout) obj;
            Context appContext = ReVancedUtils.getContext();
            if (ReVancedUtils.isTablet(appContext) || SharedPrefHelper.getBoolean(appContext, SharedPrefHelper.SharedPrefNames.YOUTUBE,"pref_xfenster_tablet", false)) {
                isTabletMode = true;
            }
        } catch (Exception e) {
            LogHelper.printException(SwipeHelper.class, "Unable to set FrameLayout", e);
        }
    }

    public static void setNextGenWatchLayout(Object obj) {
        try {
            nextGenWatchLayout = (ViewGroup) obj;
        } catch (Exception e) {
            LogHelper.printException(SwipeHelper.class, "Unable to set _nextGenWatchLayout", e);
        }
    }

    public static boolean IsControlsShown() {
        FrameLayout frameLayout;
        if (isTabletMode || (frameLayout = _frameLayout) == null || frameLayout.getVisibility() != View.VISIBLE) {
            return false;
        }
        try {
        } catch (Exception e) {
            LogHelper.printException(SwipeHelper.class, "Unable to get related_endscreen_results visibility", e);
        }
        if (_frameLayout.getChildCount() > 0) {
            return _frameLayout.getChildAt(0).getVisibility() == View.VISIBLE;
        }
        refreshLayout();
        return false;
    }

    private static void refreshLayout() {
        View findViewById;
        try {
            if (isWatchWhileFullScreen() && (findViewById = nextGenWatchLayout.findViewById(getIdentifier())) != null) {
                _frameLayout = (FrameLayout) findViewById.getParent();
                LogHelper.debug(SwipeHelper.class, "related_endscreen_results refreshed");
            }
        } catch (Exception e) {
            LogHelper.printException(SwipeHelper.class, "Unable to refresh related_endscreen_results layout", e);
        }
    }


    private static boolean isWatchWhileFullScreen() {
        if (ReVancedUtils.getPlayerType() == null) {
            return false;
        }
        return ReVancedUtils.getPlayerType() == PlayerType.WATCH_WHILE_FULLSCREEN;
    }

    private static String getViewMessage(View view) {
        try {
            String resourceName = view.getResources() != null ? view.getId() != 0 ? view.getResources().getResourceName(view.getId()) : "no_id" : "no_resources";
            return "[" + view.getClass().getSimpleName() + "] " + resourceName + "\n";
        } catch (Resources.NotFoundException unused) {
            return "[" + view.getClass().getSimpleName() + "] name_not_found\n";
        }
    }

    private static int getIdentifier() {
        Context appContext = ReVancedUtils.getContext();
        assert appContext != null;
        return appContext.getResources().getIdentifier("related_endscreen_results", "id", appContext.getPackageName());
    }
}
