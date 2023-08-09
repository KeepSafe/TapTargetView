<h1 align="center">
<img src="/.github/video.gif" width="280" height="498" alt="Video 1"/>
<img src="/.github/screenshot1.png" width="280" height="498" alt="Screenshot 1"/>
<img src="/.github/screenshot2.png" width="280" height="615" alt="Screenshot 2"/>
<img src="/.github/screenshot3.jpg" width="280" height="615" alt="Screenshot 2"/><br/>

    TapTargetView
</h1>

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.getkeepsafe.taptargetview/taptargetview/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.getkeepsafe.taptargetview/taptargetview)
[![Release](https://img.shields.io/github/tag/KeepSafe/TapTargetView.svg?label=jitpack)](https://jitpack.io/#KeepSafe/TapTargetView)


An implementation of tap targets from [Google's Material Design guidelines on feature discovery](https://material.io/archive/guidelines/growth-communications/feature-discovery.html).

 **Min SDK:** 21

 [JavaDoc](https://javadoc.jitpack.io/com/github/KeepSafe/TapTargetView/latest/javadoc/)

## Installation

TapTargetView is distributed using [MavenCentral](https://search.maven.org/artifact/com.getkeepsafe.taptargetview/taptargetview).

## No Publish

If you wish, you may also use TapTargetView with [jitpack](https://jitpack.io/#KeepSafe/TapTargetView).
For snapshots, please follow the instructions [here](https://jitpack.io/#KeepSafe/TapTargetView/-SNAPSHOT).

## Usage

### Simple usage

```kotlin
    Activity.showGuideView(
        view.createTarget("Please Input Some Thing")
        .outerCircleColor(R.color.colorAccent)
        .targetIconColor(android.R.color.holo_blue_dark)
        .transparentTarget(true)
        .textColor(android.R.color.black)
        .setTargetShapeType(TapTargetShapeType.RectAngle(16))
        )
```

```java
    TargetViewExtensionsKTX.showGuideView(
            Activity,
            view.createTarget("Please Input Some Thing")
                    .outerCircleColor(R.color.colorAccent)
                    .targetIconColor(android.R.color.holo_blue_dark)
                    .transparentTarget(true)
                    .textColor(android.R.color.black)
                    .setTargetShapeType(TapTargetShapeType.RectAngle(16)),
            null
    );
```

You may also choose to target your own custom `Rect` with `TapTarget.forBounds(Rect, ...)`

Additionally, each color can be specified via a `@ColorRes` or a `@ColorInt`. Functions that have the suffix `Int` take a `@ColorInt`.

*Tip: When targeting a Toolbar item, be careful with Proguard and ensure you're keeping certain fields. See [#180](https://github.com/KeepSafe/TapTargetView/issues/180)*

### Sequences

You can easily create a sequence of tap targets with `TapTargetSequence`:

```kotlin
new TapTargetSequence(this)
    .addTarget(
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
        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
            // Perform action for the current target
        }

        @Override
        public void onSequenceCanceled(TapTarget lastTarget) {
            // Boo
        }
    });
```

A sequence is started via a call to `start()` on the `TapTargetSequence` instance

For more examples of usage, please look at the included sample app.

### Tutorials
- [raywenderlich.com](https://www.raywenderlich.com/5194-taptargetview-for-android-tutorial)

## Third Party Bindings

### React Native
Thanks to @prscX, you may now use this library with [React Native](https://github.com/facebook/react-native) via the module [here](https://github.com/prscX/react-native-taptargetview)

### NativeScript
Thanks to @hamdiwanis, you may now use this library with [NativeScript](https://nativescript.org) via the plugin [here](https://github.com/hamdiwanis/nativescript-app-tour)

### Xamarin
Thanks to @btripp, you may now use this library via a Xamarin Binding located [here](https://www.nuget.org/packages/Xamarin.TapTargetView).

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
