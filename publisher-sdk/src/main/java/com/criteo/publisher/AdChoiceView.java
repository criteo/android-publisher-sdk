package com.criteo.publisher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class AdChoiceView extends ImageView implements OnClickListener {

    private static final int FRAME_WIDTH = 19;
    private static final int FRAME_HEIGHT = 15;

    private Context mContext;
    private String adChoiceLink;
    private String privacyOptOutImageUrl;

    public AdChoiceView(Context context) {
        super(context);
        mContext = context;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FRAME_WIDTH, FRAME_HEIGHT);
        params.gravity = Gravity.END;
        setLayoutParams(params);
        setImageResource(R.drawable.adchoice);
        setScaleType(ScaleType.FIT_CENTER);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!TextUtils.isEmpty(adChoiceLink)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(adChoiceLink));
            mContext.startActivity(browserIntent);
        }
    }

    //Create and execute ImageDownloadAsync with listener and when called back set AdChoice image
    private void setPrivacyOptOutImage() {
        if (!TextUtils.isEmpty(privacyOptOutImageUrl)) {

        }
    }
}
