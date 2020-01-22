package com.example.diaglogtest;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

public class BlurBehindDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "BlurBehindDialog";
    private Button mBtnYes;
    private Button mBtnNo;
    private AppCompatActivity mActivity;

    public BlurBehindDialog(AppCompatActivity a) {
        super(a);
        mActivity = a;
        initialize();
    }

    public BlurBehindDialog(AppCompatActivity a, @StyleRes int themeResId) {
        super(a, themeResId);
        mActivity = a;
        initialize();
    }

    private void initialize() {
        // get activity dis[lay region
        Rect activityFrame = new Rect();
        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(activityFrame);

        // blur behind
        Bitmap activityScreenShot = takeScreenShot(mActivity, activityFrame);
        long startBlurProcessingTime = SystemClock.elapsedRealtime();
        Bitmap blurBitmap = BlurBuilder.blurByRenderScript(mActivity.getBaseContext(), activityScreenShot, 25);
//        Bitmap blurBitmap = BlurBuilder.fastBlur(activityScreenShot, 10);
        Log.d(TAG, "Blur background creation duration = " + (SystemClock.elapsedRealtime() - startBlurProcessingTime));

        final Drawable draw = new BitmapDrawable(mActivity.getResources(), blurBitmap);
        Window dialogWindow = this.getWindow();
        dialogWindow.setBackgroundDrawable(draw);

        // set position
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = activityFrame.left; // The new position of the X coordinates
        lp.y = activityFrame.top; // The new position of the Y coordinates
        lp.width = activityFrame.right - activityFrame.left; // Width
        lp.height = activityFrame.bottom - activityFrame.top; // Heigh
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        mBtnYes = findViewById(R.id.btn_yes);
        mBtnNo = findViewById(R.id.btn_no);
        mBtnYes.setOnClickListener(this);
        mBtnNo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                mActivity.finish();
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private static Bitmap takeScreenShot(AppCompatActivity activity, Rect activityFrame) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        int width = activityFrame.right - activityFrame.left;
        int height = activityFrame.bottom - activityFrame.top;
        Bitmap b = Bitmap.createBitmap(b1, 0, activityFrame.top, width, height);
        view.destroyDrawingCache();
        return b;
    }
}
