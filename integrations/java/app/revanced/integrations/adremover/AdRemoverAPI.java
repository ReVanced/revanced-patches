package app.revanced.integrations.adremover;


import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toolbar;

import app.revanced.integrations.patches.HideShortsButtonPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

/**
 * API Class that provides the logic to the Patch classes. All methods in here should be protected/private and only be accessed from a Patch class.
 */
public class AdRemoverAPI {

    /**
     * Removes Reels and Home ads
     *
     * @param view
     */
    public static void HideViewWithLayout1dp(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams);
        } else if (view instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams2);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams3);
        } else if (view instanceof Toolbar) {
            Toolbar.LayoutParams layoutParams4 = new Toolbar.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams4);
        } else if (view instanceof ViewGroup) {
            ViewGroup.LayoutParams layoutParams5 = new ViewGroup.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams5);
        } else {
            LogHelper.debug(AdRemoverAPI.class, "HideViewWithLayout1dp - Id: " + view.getId() + " Type: " + view.getClass().getName());
        }
    }

    /**
     * Removes the Create button
     *
     * @param view
     */
    public static void hideCreateButton(View view) {
        String message = SettingsEnum.CREATE_BUTTON_SHOWN_BOOLEAN.getBoolean() ? "Create button: Shown" : "Create button: Hidden";
        LogHelper.debug(AdRemoverAPI.class, message);
        if (SettingsEnum.CREATE_BUTTON_SHOWN_BOOLEAN.getBoolean()) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * Removes the shorts button
     *
     * @param view
     */
    public static void hideShortsButton(View view) {
        if (HideShortsButtonPatch.lastPivotTab != null && HideShortsButtonPatch.lastPivotTab.name() == "TAB_SHORTS") {
            String message = SettingsEnum.SHORTS_BUTTON_SHOWN_BOOLEAN.getBoolean() ? "Shorts button: shown" : "Shorts button: hidden";
            LogHelper.debug(AdRemoverAPI.class, message);
            if (!SettingsEnum.SHORTS_BUTTON_SHOWN_BOOLEAN.getBoolean()) {
                view.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Removes the InfoCardSuggestions
     *
     * @param InfoCardOverlayPresenter
     * @return
     */
    public static Object removeInfoCardSuggestions(Object InfoCardOverlayPresenter) {
        if (!SettingsEnum.INFO_CARDS_SHOWN_BOOLEAN.getBoolean()) InfoCardOverlayPresenter = null;
        String message = InfoCardOverlayPresenter == null ? "RemoveInfoCardSuggestions: true" : "RemoveInfoCardSuggestions: false";
        LogHelper.debug(AdRemoverAPI.class, message);
        return InfoCardOverlayPresenter;
    }

    /**
     * Removes the Suggestions
     *
     * @param showSuggestions
     * @return
     */
    public static Boolean removeSuggestions(Boolean showSuggestions) {
        if (!SettingsEnum.SUGGESTIONS_SHOWN_BOOLEAN.getBoolean()) showSuggestions = false;
        String message = showSuggestions ? "RemoveSuggestions: true" : "RemoveSuggestions: false";
        LogHelper.debug(AdRemoverAPI.class, message);
        return showSuggestions;
    }

    /*
    private static void inspectComponentHost(Object item) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            LogHelper.debug("Litho", "Couldn't locate the method called from.");
        } else {
            String sb = "Called from method: " +
                    stackTraceElements[3].toString() + "\n";
            LogHelper.debug("Litho", sb);
        }
        if (item == null) {
            LogHelper.debug("Litho", "Item is null.");
        } else if (item.getClass().getSimpleName().contains("cwl")) {
            LogHelper.debug("Litho", "Item is a cwl item.");
            LogHelper.debug("Litho", getViewHierarchy((ViewGroup) item));
        } else {
            LogHelper.debug("Litho", "Item is not a cwl item.");
        }
    }

    private static String getViewHierarchy(ViewGroup v) {
        StringBuffer buf = new StringBuffer();
        printViews(v, buf, 0);
        return buf.toString();
    }

    private static String printViews(ViewGroup v, StringBuffer buf, int level) {
        int childCount = v.getChildCount();
        v.getId();
        indent(buf, level);
        buf.append(v.getClass().getName());
        buf.append(" children:");
        buf.append(childCount);
        buf.append("  id:").append(v.getId());
        buf.append("\n");
        for (int i = 0; i < childCount; i++) {
            View child = v.getChildAt(i);
            if (child instanceof ViewGroup) {
                printViews((ViewGroup) child, buf, level + 1);
            } else {
                indent(buf, level + 1);
                buf.append(child.getClass().getName());
                buf.append("  id:").append(child.getId());
                buf.append("\n");
            }
        }
        return buf.toString();
    }

    private static void indent(StringBuffer buf, int level) {
        for (int i = 0; i < level; i++) {
            buf.append("  ");
        }
    }

    private static void recursiveLoopChildren(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                recursiveLoopChildren((ViewGroup) child);
                child.setVisibility(View.GONE);
            } else if (child != null) {
                child.setVisibility(View.GONE);
            }
        }
    }*/

}
