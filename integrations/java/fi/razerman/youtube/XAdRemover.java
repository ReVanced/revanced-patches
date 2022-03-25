package fi.razerman.youtube;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toolbar;
import fi.razerman.youtube.preferences.BooleanPreferences;

/* loaded from: classes6.dex */
public class XAdRemover {
    public static Object RemoveInfoCardSuggestions(Object InfoCardOverlayPresenter) {
        XGlobals.ReadSettings();
        if (!XGlobals.suggestionsShown) {
            InfoCardOverlayPresenter = null;
        }
        if (XGlobals.debug) {
            if (InfoCardOverlayPresenter == null) {
                Log.d("XAdRemover", "RemoveInfoCardSuggestions: true");
            } else {
                Log.d("XAdRemover", "RemoveInfoCardSuggestions: false");
            }
        }
        return InfoCardOverlayPresenter;
    }

    public static Boolean RemoveSuggestions(Boolean showSuggestions) {
        XGlobals.ReadSettings();
        if (showSuggestions && !XGlobals.suggestionsShown) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "RemoveSuggestions: Removed");
            }
            return false;
        } else if (XGlobals.debug && showSuggestions) {
            Log.d("XAdRemover", "RemoveSuggestions: Not removed");
            return true;
        } else if (!XGlobals.debug) {
            return showSuggestions;
        } else {
            Log.d("XAdRemover", "RemoveSuggestions: Already not shown");
            return false;
        }
    }

    public static FrameLayout CheckInfoCardsStatus(FrameLayout frameLayout) {
        XGlobals.ReadSettings();
        frameLayout.setVisibility(XGlobals.infoCardsShown ? View.VISIBLE : View.GONE);
        if (XGlobals.debug) {
            Log.d("XAdRemover", "CheckInfoCardsStatus - Set visibility to: " + XGlobals.infoCardsShown);
        }
        return frameLayout;
    }

    public static boolean isBrandingWatermarkShown(boolean defaultValue) {
        XGlobals.ReadSettings();
        if (defaultValue && !XGlobals.brandingShown) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "BrandingWatermark: Removed");
            }
            return false;
        } else if (XGlobals.debug && defaultValue) {
            Log.d("XAdRemover", "BrandingWatermark: Not removed");
            return true;
        } else if (!XGlobals.debug) {
            return defaultValue;
        } else {
            Log.d("XAdRemover", "BrandingWatermark: Already not shown");
            return false;
        }
    }

    public static int BrandingWatermark(int defaultValue) {
        XGlobals.ReadSettings();
        if (defaultValue == 0 && !XGlobals.brandingShown) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "BrandingWatermark: Removed");
            }
            return 8;
        } else if (XGlobals.debug && defaultValue == 0) {
            Log.d("XAdRemover", "BrandingWatermark: Not removed");
            return defaultValue;
        } else if (!XGlobals.debug) {
            return defaultValue;
        } else {
            Log.d("XAdRemover", "BrandingWatermark: Already not shown");
            return defaultValue;
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
    }

    public static void HideViewV2(View view) {
        XGlobals.ReadSettings();
        if (!XGlobals.homeAdsShown) {
            recursiveLoopChildren((ViewGroup) view);
            RelativeLayout relativeLayout = new RelativeLayout(XGlobals.getContext());
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(1, 1);
            ((ViewGroup) view).addView(relativeLayout, rlp);
        }
    }

    public static void HideReel(View view) {
        XGlobals.ReadSettings();
        if (!XGlobals.reelShown) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "HideReel: " + view.getId());
            }
            HideViewWithLayout1dp(view);
        }
    }

    public static void HideView(View view) {
        XGlobals.ReadSettings();
        if (!XGlobals.homeAdsShown) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "HideView: " + view.getId());
            }
            HideViewWithLayout1dp(view);
        }
    }

    private static void HideViewWithLayout1dp(View view) {
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
        } else if (XGlobals.debug) {
            Log.d("XAdRemover", "HideViewWithLayout1dp - Id: " + view.getId() + " Type: " + view.getClass().getName());
        }
    }

    public static boolean VideoAdsEnabled(boolean input) {
        XGlobals.ReadSettings();
        if (XGlobals.videoAdsShown) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "Videoads: shown - " + input);
            }
            return input;
        } else if (!XGlobals.debug) {
            return false;
        } else {
            Log.d("XAdRemover", "Videoads: hidden");
            return false;
        }
    }

    public static void hideCreateButton(View view) {
        if (BooleanPreferences.isCreateButtonHidden()) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "Create button: shown");
            }
            view.setVisibility(View.GONE);
        } else if (XGlobals.debug) {
            Log.d("XAdRemover", "Create button: hidden");
        }
    }

    public static void inspectComponentHost(Object item) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("Litho", "Couldn't locate the method called from.");
        } else {
            String sb = "Called from method: " +
                    stackTraceElements[3].toString() + "\n";
            Log.d("Litho", sb);
        }
        if (item == null) {
            Log.d("Litho", "Item is null.");
        } else if (item.getClass().getSimpleName().contains("cwl")) {
            Log.d("Litho", "Item is a cwl item.");
            Log.i("Litho", getViewHierarcy((ViewGroup) item));
        } else {
            Log.d("Litho", "Item is not a cwl item.");
        }
    }

    public static String getViewHierarcy(ViewGroup v) {
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
}
