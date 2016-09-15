package com.getkeepsafe.taptargetviewsample;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Display display = getWindowManager().getDefaultDisplay();
        final Drawable droid = getResources().getDrawable(R.drawable.ic_android_black_24dp, getTheme());
        final Rect droidTarget = droid.copyBounds();
        droidTarget.offset(display.getWidth() / 2, display.getHeight() / 2);

        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.back), "This is the back button", "It allows you to go back, sometimes"),
                        TapTarget.forView(findViewById(R.id.search), "This is a search icon", "As you can see, it has gotten pretty dark around here...")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorAccent)
                                .targetCircleColor(android.R.color.black)
                                .textColor(android.R.color.black),
                        TapTarget.forBounds(droidTarget, "Oh look!", "You can point to any part of the screen")
                                .icon(droid)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        ((TextView) findViewById(R.id.educated)).setText("Congratulations! You're educated now!");
                    }

                    @Override
                    public void onSequenceCanceled() {
                        ((TextView) findViewById(R.id.educated)).setText("Uh oh! You canceled the sequence :(");
                    }
                });

        TapTargetView.showFor(this, TapTarget.forView(findViewById(R.id.fab), "Hello, world!", "This is the sample app for TapTargetView")
                .cancelable(false)
                .tintTarget(false), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                sequence.start();
            }
        });
    }
}
