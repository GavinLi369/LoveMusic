package gavin.lovemusic.localmusic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import gavin.lovemusic.constant.R;

/**
 * Created by GavinLi
 * on 4/8/17.
 */

public class FileScannerDialog extends AppCompatDialog {
    private TextView mPathView;

    public FileScannerDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scanner);
        mPathView = (TextView) findViewById(R.id.tv_path);
        ImageView findView = (ImageView) findViewById(R.id.img_find);
        assert findView != null;
        float curTranslationX = findView.getTranslationX();
        float curTranslationY = findView.getTranslationY();
        ObjectAnimator xAnimator1 = ObjectAnimator.ofFloat(findView,
                "translationX",
                curTranslationX, curTranslationX + 50);
        xAnimator1.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator xAnimator2 = ObjectAnimator.ofFloat(findView,
                "translationX",
                curTranslationX + 50, curTranslationX + 100);
        xAnimator2.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator xAnimator3 = ObjectAnimator.ofFloat(findView,
                "translationX",
                curTranslationX + 100, curTranslationX + 50);
        xAnimator3.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator xAnimator4 = ObjectAnimator.ofFloat(findView,
                "translationX",
                curTranslationX + 50, curTranslationX);
        xAnimator4.setInterpolator(new DecelerateInterpolator());
        AnimatorSet xAnimator = new AnimatorSet();
        xAnimator.play(xAnimator1);
        xAnimator.play(xAnimator2).after(xAnimator1);
        xAnimator.play(xAnimator3).after(xAnimator2);
        xAnimator.play(xAnimator4).after(xAnimator3);

        ObjectAnimator yAnimator1 = ObjectAnimator.ofFloat(findView,
                "translationY",
                curTranslationY, curTranslationY - 50);
        yAnimator1.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator yAnimator2 = ObjectAnimator.ofFloat(findView,
                "translationY",
                curTranslationY - 50, curTranslationY);
        yAnimator2.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator yAnimator3 = ObjectAnimator.ofFloat(findView,
                "translationY",
                curTranslationY, curTranslationY + 50);
        yAnimator3.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator yAnimator4 = ObjectAnimator.ofFloat(findView,
                "translationY",
                curTranslationY + 50, curTranslationY);
        yAnimator4.setInterpolator(new AccelerateInterpolator());
        AnimatorSet yAnimator = new AnimatorSet();
        yAnimator.play(yAnimator1);
        yAnimator.play(yAnimator2).after(yAnimator1);
        yAnimator.play(yAnimator3).after(yAnimator2);
        yAnimator.play(yAnimator4).after(yAnimator3);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(xAnimator);
        animatorSet.play(yAnimator).with(xAnimator);
        animatorSet.setDuration(500);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet.start();
            }
        });
    }

    public void updateFilePath(String path) {
        mPathView.setText(path);
    }
}
