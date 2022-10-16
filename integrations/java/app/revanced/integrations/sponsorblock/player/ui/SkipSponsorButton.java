package app.revanced.integrations.sponsorblock.player.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.sponsorblock.PlayerController;

public class SkipSponsorButton extends FrameLayout {
    public CharSequence skipSponsorTextViewText;
    public CharSequence skipSponsorText;
    public ImageView skipSponsorButtonIcon;
    public TextView skipSponsorTextView;
    public int currentTextColor;
    public int invertedButtonForegroundColor;
    public int backgroundColor;
    public int invertedBackgroundColor;
    public ColorDrawable backgroundColorDrawable;
    public int defaultBottomMargin;
    public int ctaBottomMargin;
    private LinearLayout skipSponsorBtnContainer;
    private final Paint background;
    private final Paint border;
    private boolean highContrast = true;

    public SkipSponsorButton(Context context) {
        super(context);
        this.background = new Paint();
        this.border = new Paint();
        this.initialize(context);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.background = new Paint();
        this.border = new Paint();
        this.initialize(context);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.background = new Paint();
        this.border = new Paint();
        this.initialize(context);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.background = new Paint();
        this.border = new Paint();
        this.initialize(context);
    }

    private final void initialize(Context context) {
        LayoutInflater.from(context).inflate(getIdentifier(context, "skip_sponsor_button", "layout"), this, true);  // layout:skip_ad_button
        this.setMinimumHeight(this.getResources().getDimensionPixelSize(getIdentifier(context, "ad_skip_ad_button_min_height", "dimen")));  // dimen:ad_skip_ad_button_min_height
        this.skipSponsorBtnContainer = (LinearLayout) this.findViewById(getIdentifier(context, "skip_sponsor_button_container", "id"));  // id:skip_ad_button_container
        this.skipSponsorButtonIcon = (ImageView) this.findViewById(getIdentifier(context, "skip_sponsor_button_icon", "id"));  // id:skip_ad_button_icon
        this.backgroundColor = getColor(context, getIdentifier(context, "skip_ad_button_background_color", "color"));  // color:skip_ad_button_background_color
        this.invertedBackgroundColor = getColor(context, getIdentifier(context, "skip_ad_button_inverted_background_color", "color"));  // color:skip_ad_button_inverted_background_color
        this.background.setColor(this.backgroundColor);
        this.background.setStyle(Paint.Style.FILL);
        int borderColor = getColor(context, getIdentifier(context, "skip_ad_button_border_color", "color"));  // color:skip_ad_button_border_color
        this.border.setColor(borderColor);
        float borderWidth = this.getResources().getDimension(getIdentifier(context, "ad_skip_ad_button_border_width", "dimen"));  // dimen:ad_skip_ad_button_border_width
        this.border.setStrokeWidth(borderWidth);
        this.border.setStyle(Paint.Style.STROKE);
        TextView skipSponsorText = (TextView) this.findViewById(getIdentifier(context, "skip_sponsor_button_text", "id"));  // id:skip_ad_button_text
        this.skipSponsorTextView = skipSponsorText;
        this.skipSponsorTextViewText = skipSponsorText.getText();
        this.currentTextColor = this.skipSponsorTextView.getCurrentTextColor();
        this.invertedButtonForegroundColor = getColor(context, getIdentifier(context, "skip_ad_button_inverted_foreground_color", "color"));  // color:skip_ad_button_inverted_foreground_color
        this.backgroundColorDrawable = new ColorDrawable(this.backgroundColor);
        Resources resources = context.getResources();
        this.defaultBottomMargin = resources.getDimensionPixelSize(getIdentifier(context, "skip_button_default_bottom_margin", "dimen"));  // dimen:skip_button_default_bottom_margin
        this.ctaBottomMargin = resources.getDimensionPixelSize(getIdentifier(context, "skip_button_cta_bottom_margin", "dimen"));  // dimen:skip_button_cta_bottom_margin
        this.skipSponsorText = resources.getText(getIdentifier(context, "skip_sponsor", "string"));  // string:skip_ads "Skip ads"

        this.skipSponsorBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.debug(SkipSponsorButton.class, "Skip button clicked");
                PlayerController.onSkipSponsorClicked();
            }
        });
    }

    @Override  // android.view.ViewGroup
    protected final void dispatchDraw(Canvas canvas) {
        int width = this.skipSponsorBtnContainer.getWidth();
        int height = this.skipSponsorBtnContainer.getHeight();
        int top = this.skipSponsorBtnContainer.getTop();
        int left = this.skipSponsorBtnContainer.getLeft();
        float floatLeft = (float) left;
        float floatTop = (float) top;
        float floatWidth = (float) (left + width);
        float floatHeight = (float) (top + height);
        canvas.drawRect(floatLeft, floatTop, floatWidth, floatHeight, this.background);
        if (!this.highContrast) {
            canvas.drawLines(new float[]{floatWidth, floatTop, floatLeft, floatTop, floatLeft, floatTop, floatLeft, floatHeight, floatLeft, floatHeight, floatWidth, floatHeight}, this.border);
        }

        super.dispatchDraw(canvas);
    }


    public static int getColor(Context context, int arg3) {
        return context.getColor(arg3);
    }

    private int getIdentifier(Context context, String name, String defType) {
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }
}
