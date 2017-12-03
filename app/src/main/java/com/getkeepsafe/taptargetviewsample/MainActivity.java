package com.getkeepsafe.taptargetviewsample;

import android.content.DialogInterface;
import android.graphics.Rect;
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
import android.util.Log;
import android.view.Display;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;

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
                TapTarget.forToolbarNavigationIcon(toolbar)
                    .titleText("This is the back button")
                    .descriptionText(sassyDesc)
                    .id("back")
                    .build(),
                // Likewise, this tap target will target the search button
                TapTarget.forToolbarMenuItem(toolbar, R.id.search)
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
                TapTarget.forToolbarOverflow(toolbar)
                    .titleText("This will show more options")
                    .descriptionText("But they're not useful :(")
                    .id("more options")
                    .build(),
                // This tap target will target our droid buddy at the given target rect
                TapTarget.forBounds(this, droidTarget)
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
                TapTarget.forView(dialog.getButton(DialogInterface.BUTTON_POSITIVE))
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
        TapTarget.forView(findViewById(R.id.fab))
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
}
