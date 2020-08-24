package pl.jakubweg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

public class NewSegmentHelperLayout extends LinearLayout implements View.OnClickListener {
    private static final int rewindBtnId = 1235;
    private static final int forwardBtnId = 1236;
    private static final int publishBtnId = 1237;
    private static final int hideBtnId = 1238;
    private static final int markLocationBtnId = 1239;
    private static final int previewBtnId = 1240;
    private static final int editByHandBtnId = 1241;
    private static WeakReference<NewSegmentHelperLayout> INSTANCE = new WeakReference<>(null);
    private static boolean isShown = false;
    private final int padding;
    private final int iconSize;
    private final int rippleEffectId;
    private final String packageName;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public NewSegmentHelperLayout(Context context) {
        super(context);
        INSTANCE = new WeakReference<>(this);
        isShown = false;
        setVisibility(GONE);

        packageName = context.getPackageName();
        padding = (int) SkipSegmentView.convertDpToPixel(4f, context);
        iconSize = (int) SkipSegmentView.convertDpToPixel(40f, context);

        TypedValue rippleEffect = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleEffect, true);
        rippleEffectId = rippleEffect.resourceId;


        setOrientation(VERTICAL);
        @SuppressLint("RtlHardcoded")
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.START | Gravity.LEFT | Gravity.CENTER_VERTICAL
        );
        this.setBackgroundColor(0x66000000);
        this.bringToFront();
        this.setLayoutParams(layoutParams);
        this.setPadding(padding, padding, padding, padding);

        final LinearLayout topLayout = new LinearLayout(context);
        final LinearLayout bottomLayout = new LinearLayout(context);
        topLayout.setOrientation(HORIZONTAL);
        bottomLayout.setOrientation(HORIZONTAL);
        this.addView(topLayout);
        this.addView(bottomLayout);

        topLayout.addView(createTextViewBtn(rewindBtnId, "player_fast_rewind"));
        topLayout.addView(createTextViewBtn(forwardBtnId, "player_fast_forward"));
        topLayout.addView(createTextViewBtn(markLocationBtnId, "ic_sb_adjust"));
        bottomLayout.addView(createTextViewBtn(previewBtnId, "ic_sb_compare"));
        bottomLayout.addView(createTextViewBtn(editByHandBtnId, "ic_sb_edit"));
        bottomLayout.addView(createTextViewBtn(publishBtnId, "ic_sb_publish"));
//        bottomLayout.addView(createTextViewBtn(hideBtnId,"btn_close_light"));
    }

    public static void show() {
        if (isShown) return;
        isShown = true;
        NewSegmentHelperLayout i = INSTANCE.get();
        if (i == null) return;
        i.setVisibility(VISIBLE);
        i.bringToFront();
        i.requestLayout();
        i.invalidate();
    }

    public static void hide() {
        if (!isShown) return;
        isShown = false;
        NewSegmentHelperLayout i = INSTANCE.get();
        if (i != null)
            i.setVisibility(GONE);
    }

    public static void toggle() {
        if (isShown) hide();
        else show();
    }

    private View createTextViewBtn(int id, String drawableName) {
        int drawableId = getResources().getIdentifier(drawableName, "drawable", packageName);
        final ImageView view = new ImageView(getContext());
        view.setPadding(padding, padding, padding, padding);
        view.setLayoutParams(new LayoutParams(iconSize, iconSize, 1));
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setImageResource(drawableId);
        view.setId(id);
        view.setClickable(true);
        view.setFocusable(true);
        view.setBackgroundResource(rippleEffectId);
        view.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case forwardBtnId:
                PlayerController.skipRelativeMilliseconds(SponsorBlockSettings.adjustNewSegmentMillis);
                break;
            case rewindBtnId:
                PlayerController.skipRelativeMilliseconds(-SponsorBlockSettings.adjustNewSegmentMillis);
                break;
            case markLocationBtnId:
                SponsorBlockUtils.onMarkLocationClicked(getContext());
                break;
            case publishBtnId:
                SponsorBlockUtils.onPublishClicked(getContext());
                break;
            case previewBtnId:
                SponsorBlockUtils.onPreviewClicked(getContext());
                break;
            case editByHandBtnId:
                SponsorBlockUtils.onEditByHandClicked(getContext());
                break;
            case hideBtnId:
                hide();
                break;
        }
    }
}
