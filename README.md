<h1 align="center">
<img src="/.github/video.gif" width="280" height="498" alt="Video 1"/>
<img src="/.github/screenshot1.png" width="280" height="498" alt="Screenshot 1"/>
<img src="/.github/screenshot2.png" width="280" height="498" alt="Screenshot 2"/><br/>

    TapTargetView
</h1>

[![Download](https://api.bintray.com/packages/keepsafesoftware/Android/TapTargetView/images/download.svg) ](https://bintray.com/keepsafesoftware/Android/TapTargetView/_latestVersion)


An implementation of tap targets from [Google's Material Design guidelines on feature discovery](https://material.google.com/growth-communications/feature-discovery.html#feature-discovery-design).

**Min SDK:** 14

## Installation

TapTargetView is distributed using [jcenter](https://bintray.com/keepsafesoftware/Android/TapTargetView/view).

```groovy
   repositories { 
        jcenter()
   }
   
   dependencies {
         compile 'com.getkeepsafe.taptargetview:taptargetview:1.6.1'
   }
```

If you wish to use a snapshot, please follow the instructions [here](https://jitpack.io/#KeepSafe/TapTargetView/-SNAPSHOT)

## Usage

### Simple usage

```java
TapTargetView.showFor(this,                 // `this` is an Activity
    TapTarget.forView(findViewById(R.id.target), "This is a target", "We have the best targets, believe me")
        // All options below are optional
        .outerCircleColor(R.color.red)      // Specify a color for the outer circle
        .targetCircleColor(R.color.white)   // Specify a color for the target circle
        .titleTextSize(20)                  // Specify the size (in sp) of the title text
        .titleTextColor(R.color.white)      // Specify the color of the title text
        .descriptionTextSize(10)            // Specify the size (in sp) of the description text
        .descriptionTextColor(R.color.red)  // Specify the color of the description text
        .textColor(R.color.blue)            // Specify a color for both the title and description text
        .textTypeFace(Typeface.SANS_SERIF)  // Specify a typeface for the text
        .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
        .drawShadow(true)                   // Whether to draw a drop shadow or not
        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
        .tintTarget(true)                   // Whether to tint the target view's color
        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
        .icon(Drawable)                     // Specify a custom drawable to draw as the target
        .targetRadius(60),                  // Specify the target radius (in dp)
    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
        @Override
        public void onTargetClick(TapTargetView view) {
            super.onTargetClick(view);      // This call is optional
            doSomething();
        }
    });
```

You may also choose to target your own custom `Rect` with `TapTarget.forBounds(Rect, ...)`

Additionally, each color can be specified via a `@ColorRes` or a `@ColorInt`. Functions that have the suffix `Int` take a `@ColorInt`.

### Sequences

You can easily create a sequence of tap targets with `TapTargetSequence`:

```java
new TapTargetSequence(this)
    .targets(
        TapTarget.forView(findViewById(R.id.never), "Gonna"),
        TapTarget.forView(findViewById(R.id.give), "You", "Up")
                .dimColor(android.R.color.never)
                .outerCircleColor(R.color.gonna)
                .targetCircleColor(R.color.let)
                .textColor(android.R.color.you),
        TapTarget.forBounds(rickTarget, "Down", ":^)")
                .cancelable(false)
                .icon(rick))
    .listener(new TapTargetSequence.Listener() {
        // This listener will tell us when interesting(tm) events happen in regards
        // to the sequence
        @Override
        public void onSequenceFinish() {
            // Yay
        }

        @Override
        public void onSequenceCanceled(TapTarget lastTarget) {
            // Boo
        }
    });
```

A sequence is started via a call to `start()` on the `TapTargetSequence` instance

For more examples of usage, please look at the included sample app.

## License

    Copyright 2016 Keepsafe Software Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
