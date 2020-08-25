package pl.jakubweg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import static pl.jakubweg.PlayerController.VERBOSE;
import static pl.jakubweg.StringRef.str;

@SuppressLint({"RtlHardcoded", "SetTextI18n", "LongLogTag", "AppCompatCustomView"})
public class SkipSegmentView extends TextView implements View.OnClickListener {
    public static final String TAG = "jakubweg.SkipSegmentView";
    private static boolean isVisible = false;
    private static WeakReference<SkipSegmentView> view = new WeakReference<>(null);
    private static SponsorSegment lastNotifiedSegment;

    public SkipSegmentView(Context context) {
        super(context);
        isVisible = false;
        setVisibility(GONE);
        view = new WeakReference<>(this);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.RIGHT | Gravity.CENTER_VERTICAL
        );
        this.setLayoutParams(layoutParams);
        this.setBackgroundColor(0x66000000);
//        this.setBackgroundColor(Color.MAGENTA);
        this.setTextColor(0xFFFFFFFF);
        int padding = (int) convertDpToPixel(4, context);
        setPadding(padding, padding, padding, padding);

        this.setText(str("tap_skip"));

        setOnClickListener(this);
    }

    public static void show() {
        if (isVisible) return;
        SkipSegmentView view = SkipSegmentView.view.get();
        if (VERBOSE)
            Log.d(TAG, "show; view=" + view);
        if (view != null) {
            view.setVisibility(VISIBLE);
            view.bringToFront();
            view.requestLayout();
            view.invalidate();
        }
        isVisible = true;
    }

    public static void hide() {
        if (!isVisible) return;
        SkipSegmentView view = SkipSegmentView.view.get();
        if (VERBOSE)
            Log.d(TAG, "hide; view=" + view);
        if (view != null)
            view.setVisibility(GONE);
        isVisible = false;
    }

    public static void notifySkipped(SponsorSegment segment) {
        if (segment == lastNotifiedSegment) {
            if (VERBOSE)
                Log.d(TAG, "notifySkipped; segment == lastNotifiedSegment");
            return;
        }
        lastNotifiedSegment = segment;
        String skipMessage = segment.category.skipMessage.toString();
        SkipSegmentView view = SkipSegmentView.view.get();
        if (VERBOSE)
            Log.d(TAG, String.format("notifySkipped; view=%s, message=%s", view, skipMessage));
        if (view != null)
            Toast.makeText(view.getContext(), skipMessage, Toast.LENGTH_SHORT).show();
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    public void onClick(View v) {
        PlayerController.onSkipSponsorClicked();
    }
}
