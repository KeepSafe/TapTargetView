package com.getkeepsafe.taptargetviewsample;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.BoundsTapTarget;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.getkeepsafe.taptargetview.ToolbarTapTarget;
import com.getkeepsafe.taptargetview.ViewTapTarget;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.inflateMenu(R.menu.menu_main);
    toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));

    // We load a drawable and create a location to show a tap target here
    // We need the display to get the width and height at this point in time
    final Display display = getWindowManager().getDefaultDisplay();
    // Load our little droid guy
    final Drawable droid = ContextCompat.getDrawable(this, R.drawable.ic_android_black_24dp);
    // Tell our droid buddy where we want him to appear
    final Rect droidTarget =
        new Rect(0, 0, droid.getIntrinsicWidth() * 2, droid.getIntrinsicHeight() * 2);
    // Using deprecated methods makes you look way cool
    droidTarget.offset(display.getWidth() / 2, display.getHeight() / 2);

    final SpannableString sassyDesc = new SpannableString("It allows you to go back, sometimes");
    final int sassyLength = sassyDesc.length();
    sassyDesc.setSpan(
        new StyleSpan(Typeface.ITALIC), sassyLength - "sometimes".length(), sassyLength, 0);

    // We have a sequence of targets, so lets build it!
    final TapTargetSequence sequence =
        new TapTargetSequence(this)
            .targets(
                // This tap target will target the back button, we just need to pass its containing
                // toolbar
                ToolbarTapTarget.ofNavigationIcon(toolbar)
                    .titleText("This is the back button")
                    .descriptionText(sassyDesc)
                    .id("back")
                    .build(),
                // Likewise, this tap target will target the search button
                ToolbarTapTarget.ofMenuItem(toolbar, R.id.search)
                    .titleText("This is a search icon")
                    .titleTextColorRes(android.R.color.black)
                    .descriptionText("As you can see, it has gotten pretty dark around here...")
                    .descriptionTextColorRes(android.R.color.black)
                    .dimColorRes(android.R.color.black)
                    .outerCircleColorRes(R.color.colorAccent)
                    .targetCircleColorRes(android.R.color.black)
                    .targetCircleIsTransparent(true)
                    .id("search")
                    .build(),
                // You can also target the overflow button in your toolbar
                ToolbarTapTarget.ofOverflow(toolbar)
                    .titleText("This will show more options")
                    .descriptionText("But they're not useful :(")
                    .id("more options")
                    .build(),
                // This tap target will target our droid buddy at the given target rect
                BouncyTapTarget.of(this)
                    .titleText("Oh look!")
                    .descriptionText(
                        "You can point to any part of the screen. You also can't cancel this one!")
                    .cancelable(false)
                    .icon(droid)
                    .id("droid")
                    .build())
        .listener(new TapTargetSequence.Listener() {
          // This listener will tell us when interesting(tm) events happen in regards
          // to the sequence
          @Override
          public void onSequenceFinish() {
            ((TextView) findViewById(R.id.educated)).setText(
                "Congratulations! You're educated now!");
          }

          @Override
          public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
            Log.d("TapTargetView", "Clicked on " + lastTarget.id());
          }

          @Override
          public void onSequenceCanceled(TapTarget lastTarget) {
            final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Uh oh")
                .setMessage("You canceled the sequence")
                .setPositiveButton("Oops", null).show();
            TapTargetView.showFor(
                MainActivity.this,
                ViewTapTarget.of(dialog.getButton(DialogInterface.BUTTON_POSITIVE))
                    .titleText("Uh oh!")
                    .descriptionText("You canceled the sequence at step " + lastTarget.id())
                    .cancelable(false)
                    .tintTarget(false)
                    .build(),
                new TapTargetView.BaseListener() {
                  @Override
                  public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    dialog.dismiss();
                  }
                });
          }
        });

    // You don't always need a sequence, and for that there's a single time tap target
    final SpannableString spannedDesc =
        new SpannableString("This is the sample app for TapTargetView");
    final int spannedLength = spannedDesc.length();
    spannedDesc.setSpan(
        new UnderlineSpan(), spannedLength - "TapTargetView".length(), spannedLength, 0);
    TapTargetView.showFor(
        this,
//        ViewTapTarget.of(findViewById(R.id.fab))
        BouncyTapTarget.of(this)
            .titleText("Hello, world!")
            .descriptionText(spannedDesc)
            .cancelable(false)
            .shadow(true)
            .titleTextSizeDimen(R.dimen.title_text_size)
            .tintTarget(false)
            .build(),
        new TapTargetView.BaseListener() {
          @Override
          public void onTargetClick(TapTargetView view) {
            super.onTargetClick(view);
            // .. which evidently starts the sequence we defined earlier
            sequence.start();
          }

          @Override
          public void onOuterCircleClick(TapTargetView view) {
            super.onOuterCircleClick(view);
            Toast.makeText(
                view.getContext(), "You clicked the outer circle!", Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
            Log.d("TapTargetViewSample", "You dismissed me :(");
          }
        });
  }

  public static class BouncyTapTarget extends TapTarget {
    private final Rect screenBounds;

    private final TimeAnimator animator = new TimeAnimator();
    private final Rect bounds = new Rect();
    private final Random random = new Random();
    private final Vec2 position = new Vec2(0f, 0f);
    private final Vec2 direction = new Vec2(200f, 200f);

    private final TimeAnimator.TimeListener timeListener = new TimeAnimator.TimeListener() {
      @Override
      public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        direction.addTo(position, deltaTime / 1000f);
        if (position.x >= screenBounds.right || position.x <= screenBounds.left) {
          direction.x *= -1f;
        }

        if (position.y >= screenBounds.bottom || position.y <= screenBounds.top) {
          direction.y *= -1f;
        }

        int left = (int) position.x;
        int top = (int) position.y;
        bounds.set(left, top, left + 10, top + 10);
        setBounds(bounds);
      }
    };

    public static BouncyTapTarget.Builder of(Context context) {
      DisplayMetrics metrics = context.getResources().getDisplayMetrics();
      return new Builder(context, new Rect(0, 0, metrics.widthPixels, metrics.heightPixels));
    }

    BouncyTapTarget(Rect screenBounds, Parameters parameters) {
      super(parameters);
      this.screenBounds = screenBounds;
      setRandomPositionIn(random, position, screenBounds);
    }

    @Override
    public void attach(TapTargetView parent) {
      super.attach(parent);
      animator.start();
      animator.setTimeListener(timeListener);
    }

    @Override
    public void detach() {
      super.detach();
      animator.end();
      animator.setTimeListener(null);
    }

    private static void setRandomPositionIn(Random random, Vec2 dst, Rect bounds) {
      int left = random.nextInt(bounds.width()) + bounds.left;
      int top = random.nextInt(bounds.height()) + bounds.top;
      dst.x = left;
      dst.y = top;
    }

    private static class Vec2 {
      public float x;
      public float y;

      public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
      }

      public void addTo(Vec2 dst, float dt) {
        dst.x += x * dt;
        dst.y += y * dt;
      }
    }

    public static class Builder extends TapTarget.Builder {
      private final Rect screenBounds;

      Builder(Context context, Rect screenBounds) {
        super(context);
        if (screenBounds == null) {
          throw new IllegalArgumentException("Given null rect for screen bounds");
        }
        this.screenBounds = screenBounds;
      }

      @Override
      public BouncyTapTarget build() {
        return new BouncyTapTarget(screenBounds, parameters);
      }
    }
  }
}
