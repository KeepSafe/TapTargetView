package com.getkeepsafe.taptargetviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTargetView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new TapTargetView.Builder(this)
                .title("Hello, world!")
                .description("This is the sample app for TapTargetView")
                .tintTarget(false)
                .listener(new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        educateBackButton();
                    }

                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        super.onTargetCancel(view);
                        educateBackButton();
                    }
                }).showFor(findViewById(R.id.fab));
    }

    private void educateBackButton() {
        new TapTargetView.Builder(MainActivity.this)
                .title("This is the back button")
                .description("It allows you to go back, sometimes.")
                .listener(new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        educateSearchButton();
                    }
                })
                .showFor(findViewById(R.id.back));
    }

    private void educateSearchButton() {
        new TapTargetView.Builder(MainActivity.this)
                .title("This is a search icon")
                .description("As you can see, it has gotten pretty dark around here...")
                .dimColor(android.R.color.black)
                .outerCircleColor(R.color.colorAccent)
                .targetCircleColor(android.R.color.black)
                .textColor(android.R.color.black)
                .listener(new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        ((TextView) findViewById(R.id.educated)).setText("Congratulations! You're educated now!");
                    }
                })
                .showFor(findViewById(R.id.search));
    }
}
