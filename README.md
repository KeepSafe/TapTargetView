<h1 align="center">
<img src="/art/video.gif" width="280" height="498" alt="Video 1"/>
<img src="/art/screenshot1.png" width="280" height="498" alt="Screenshot 1"/>
<img src="/art/screenshot2.png" width="280" height="498" alt="Screenshot 2"/><br/>

    TapTargetView
</h1>

[![Download](https://api.bintray.com/packages/keepsafesoftware/Android/TapTargetView/images/download.svg) ](https://bintray.com/keepsafesoftware/Android/TapTargetView/_latestVersion)


An implementation of tap targets from [Google's Material Design guidelines on feature discovery](https://material.google.com/growth-communications/feature-discovery.html#feature-discovery-design).

 **Min SDK:** 15

## Installation

TapTargetView is distributed using [jcenter](https://bintray.com/keepsafesoftware/Android/TapTargetView/view).

```groovy
   repositories { 
        jcenter()
   }
   
   dependencies {
         compile 'com.getkeepsafe.taptargetview:taptargetview:1.0.0'
   }
```

## Usage

TapTargetView utilizes a builder to configure how it looks and behaves. Here is the full list of options when using TapTargetView. The only required options are specifying a title, description and target.

```java
new TapTargetView.Builder(Activity) // The activity that hosts this view
        .title(@StringRes int) // Specify the title text
        .title(String)
        .description(@StringRes int) // Specify the description text
        .description(String)
        .listener(Listener) // Specify a listener that can listen for clicks and long clicks
        .outerCircleColor(@ColorRes int)  // Specify a color for the outer circle
        .targetCircleColor(@ColorRes int) // Specify a color for the inner circle surrounding the target view 
        .textColor(@ColorRes int) // Specify a color for the text
        .textTypeface(Typeface) // Specify a custom typeface to use for the text
        .dimColor(@ColorRes int) // If set, will dim behind the view with 30% opacity of the given color
        .tintTarget(boolean) // Whether to tint the target view's color
        .drawShadow(boolean) // Whether to draw the drop shadow
        .cancelable(boolean) // Whether tapping outside the outer circle dismisses the view
        .showFor(targetView);
```

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
