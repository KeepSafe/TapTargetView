package com.getkeepsafe.taptargetviewsample

import android.content.DialogInterface
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.target.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.getkeepsafe.taptargetview.createNavigationIcon
import com.getkeepsafe.taptargetview.createOverflow
import com.getkeepsafe.taptargetview.createTarget
import com.getkeepsafe.taptargetview.forToolbarMenuItem
import com.getkeepsafe.taptargetview.showGuideView
import com.getkeepsafe.taptargetview.target.TapTargetShapeType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.navigationIcon =
            ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)

        // We load a drawable and create a location to show a tap target here
        // We need the display to get the width and height at this point in time
        val display = windowManager.defaultDisplay
        // Load our little droid guy
        val droid = ContextCompat.getDrawable(this, R.drawable.ic_android_black_24dp)
        // Tell our droid buddy where we want him to appear
        val droidTarget = Rect(0, 0, droid!!.intrinsicWidth * 2, droid.intrinsicHeight * 2)
        // Using deprecated methods makes you look way cool
        droidTarget.offset(display.width / 2, display.height / 2)
        val sassyDesc = SpannableString("It allows you to go back, sometimes")
        sassyDesc.setSpan(
            StyleSpan(Typeface.ITALIC),
            sassyDesc.length - "sometimes".length,
            sassyDesc.length,
            0
        )

        // We have a sequence of targets, so lets build it!
        val sequence = TapTargetSequence(this)
            .addTarget( // This tap target will target the back button, we just need to pass its containing toolbar
                toolbar.createNavigationIcon("This is the back button", sassyDesc)
                    .id(1),  // Likewise, this tap target will target the search button
                toolbar.forToolbarMenuItem(
                    R.id.search,
                    "This is a search icon",
                    "As you can see, it has gotten pretty dark around here..."
                )
                    .dimColor(android.R.color.black)
                    .outerCircleColor(R.color.colorAccent)
                    .targetIconColor(android.R.color.black)
                    .transparentTarget(true)
                    .textColor(android.R.color.black)
                    .id(2),  // You can also target the overflow button in your toolbar
                toolbar.createOverflow(
                    "This will show more options",
                    "But they're not useful :("
                ).id(3),  // This tap target will target our droid buddy at the given target rect
                droid.createTarget(
                    droidTarget,
                    "Oh look!",
                    "You can point to any part of the screen. You also can't cancel this one!"
                )
                    .cancelable(false)
                    .id(4)
            )
            .listener(object : TapTargetSequence.Listener {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                override fun onSequenceFinish() {
                    (findViewById<View>(R.id.educated) as TextView).text =
                        "Congratulations! You're educated now!"
                }

                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    Log.d("TapTargetView", "Clicked on " + lastTarget!!.id())
                }

                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    val dialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle("Uh oh")
                        .setMessage("You canceled the sequence")
                        .setPositiveButton("Oops", null).show()
                    dialog.showGuideView(
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).createTarget(
                            "Uh oh!",
                            "You canceled the sequence at step " + lastTarget?.id()
                        )
                            .cancelable(false)
                            .tintTarget(false), object : TapTargetView.Listener() {
                            override fun onTargetClick(view: TapTargetView) {
                                super.onTargetClick(view)
                                dialog.dismiss()
                            }
                        })
                }
            })

        // You don't always need a sequence, and for that there's a single time tap target
        val spannedDesc = SpannableString("This is the sample app for TapTargetView")
        spannedDesc.setSpan(
            UnderlineSpan(),
            spannedDesc.length - "TapTargetView".length,
            spannedDesc.length,
            0
        )
        this.showGuideView(
            findViewById<View>(R.id.fab).createTarget("Hello, world!", spannedDesc)
                .cancelable(false)
                .drawShadow(true)
                .titleTextDimen(R.dimen.title_text_size)
                .tintTarget(false), object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    // .. which evidently starts the sequence we defined earlier
                    sequence.start()
                }

                override fun onOuterCircleClick(view: TapTargetView) {
                    super.onOuterCircleClick(view)
                    Toast.makeText(
                        view.context,
                        "You clicked the outer circle!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onTargetDismissed(view: TapTargetView, userInitiated: Boolean) {
                    Log.d("TapTargetViewSample", "You dismissed me :(")
                }
            })

        val ed = findViewById<View>(R.id.ed)
        ed.setOnClickListener {
            showGuideView(
                ed.createTarget("Please Input Some Thing", "Hello Some One")
                    .outerCircleColor(R.color.colorAccent)
                    .targetIconColor(android.R.color.holo_blue_dark)
                    .transparentTarget(true)
                    .textColor(android.R.color.black)
                    .setTargetShapeType(TapTargetShapeType.RectAngle(16)),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        Toast.makeText(
                            view.context,
                            "You clicked the target! yes!!!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        super.onTargetClick(view)
                    }
                }
            )
        }
    }

}