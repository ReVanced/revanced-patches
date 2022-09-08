## 🧩 Patches

The official Patch bundle provided by ReVanced and the community.

> Looking for the JSON variant of this? [Click here](patches.json).

### 📦 `com.twitter.android`
<details>

| 💊 Patch | 📜 Description | 🏹 Target Version |
|:--------:|:--------------:|:-----------------:|
| `timeline-ads` | Removes ads from the Twitter timeline. | all |
</details>

### 📦 `com.reddit.frontpage`
<details>

| 💊 Patch | 📜 Description | 🏹 Target Version |
|:--------:|:--------------:|:-----------------:|
| `premium-icon-reddit` | Unlocking Premium Icons in reddit app. | all |
| `general-reddit-ads` | Removes general ads from the Reddit frontpage and subreddits. | all |
</details>

### 📦 `com.garzotto.pflotsh.ecmwf_a`
<details>

| 💊 Patch | 📜 Description | 🏹 Target Version |
|:--------:|:--------------:|:-----------------:|
| `pflotsh-ecmwf-subscription-unlock` | Unlocks all subscription features. | 3.5.4 |
</details>

### 📦 `com.google.android.apps.youtube.music`
<details>

| 💊 Patch | 📜 Description | 🏹 Target Version |
|:--------:|:--------------:|:-----------------:|
| `minimized-playback-music` | Enables minimized playback on Kids music. | 5.22.54 |
| `tasteBuilder-remover` | Removes the "Tell us which artists you like" card from the home screen. | 5.22.54 |
| `hide-get-premium` | Removes all "Get Premium" evidences from the avatar menu. | 5.22.54 |
| `compact-header` | Hides the music category bar at the top of the homepage. | 5.22.54 |
| `upgrade-button-remover` | Removes the upgrade tab from the pivot bar. | 5.22.54 |
| `background-play` | Enables playing music in the background. | 5.22.54 |
| `music-microg-support` | Allows YouTube Music ReVanced to run without root and under a different package name. | 5.22.54 |
| `music-video-ads` | Removes ads in the music player. | 5.22.54 |
| `codecs-unlock` | Adds more audio codec options. The new audio codecs usually result in better audio quality. | 5.22.54 |
| `exclusive-audio-playback` | Enables the option to play music without video. | 5.22.54 |
</details>

### 📦 `de.dwd.warnapp`
<details>

| 💊 Patch | 📜 Description | 🏹 Target Version |
|:--------:|:--------------:|:-----------------:|
| `promo-code-unlock` | Disables the validation of promo code. Any code will work to unlock all features. | all |
</details>

### 📦 `com.ss.android.ugc.trill`
<details>

| 💊 Patch | 📜 Description | 🏹 Target Version |
|:--------:|:--------------:|:-----------------:|
| `tiktok-download` | Remove restrictions on downloads video. | all |
| `tiktok-seekbar` | Show progress bar for all video. | all |
| `tiktok-ads` | Removes ads from TikTok. | all |
</details>

### 📦 `com.google.android.youtube`
<details>

| 💊 Patch | 📜 Description | 🏹 Target Version |
|:--------:|:--------------:|:-----------------:|
| `swipe-controls` | Adds volume and brightness swipe controls. | 17.33.42 |
| `downloads` | Enables downloading music and videos from YouTube. | 17.33.42 |
| `seekbar-tapping` | Enables tap-to-seek on the seekbar of the video player. | 17.33.42 |
| `amoled` | Enables pure black theme. | all |
| `disable-create-button` | Hides the create button in the navigation bar. | 17.33.42 |
| `hide-cast-button` | Hides the cast button in the video player. | all |
| `return-youtube-dislike` | Shows the dislike count of videos using the Return YouTube Dislike API. | 17.33.42 |
| `hide-autoplay-button` | Hides the autoplay button in the video player. | 17.33.42 |
| `premium-heading` | Shows premium branding on the home screen. | all |
| `custom-branding` | Changes the YouTube launcher icon and name to your choice (defaults to ReVanced). | all |
| `disable-fullscreen-panels` | Disables video description and comments panel in fullscreen view. | 17.33.42 |
| `old-quality-layout` | Enables the original quality flyout menu. | 17.33.42 |
| `theme` | Enables a custom theme. | all |
| `hide-shorts-button` | Hides the shorts button on the navigation bar. | 17.33.42 |
| `hide-watermark` | Hides creator's watermarks on videos. | 17.33.42 |
| `sponsorblock` | Integrate SponsorBlock. | 17.33.42 |
| `enable-wide-searchbar` | Replaces the search icon with a wide search bar. This will hide the YouTube logo when active. | 17.33.42 |
| `tablet-mini-player` | Enables the tablet mini player layout. | 17.33.42 |
| `minimized-playback` | Enables minimized and background playback. | 17.33.42 |
| `client-spoof` | Spoofs the YouTube or Vanced client to prevent playback issues. | all |
| `custom-video-buffer` | Lets you change the buffers of videos. | 17.33.42 |
| `always-autorepeat` | Always repeats the playing video again. | 17.33.42 |
| `microg-support` | Allows YouTube ReVanced to run without root and under a different package name with Vanced MicroG. | 17.33.42 |
| `settings` | Adds settings for ReVanced to YouTube. | all |
| `enable-debugging` | Enables app debugging by patching the manifest file. | all |
| `custom-playback-speed` | Adds more video playback speed options. | 17.33.42 |
| `hdr-auto-brightness` | Makes the brightness of HDR videos follow the system default. | 17.33.42 |
| `remember-video-quality` | Adds the ability to remember the video quality you chose in the video quality flyout. | 17.33.42 |
| `video-ads` | Removes ads in the video player. | 17.33.42 |
| `general-ads` | Removes general ads. | 17.33.42 |
| `hide-infocard-suggestions` | Hides infocards in videos. | 17.33.42 |
</details>



## 📝 JSON Format

This section explains the JSON format for the [patches.json](patches.json) file.

The file contains an array of objects, each object representing a patch. The object contains the following properties:

| key                           | description                                                                                                                                                                           |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `name`                        | The name of the patch.                                                                                                                                                                |
| `description`                 | The description of the patch.                                                                                                                                                         |
| `version`                     | The version of the patch.                                                                                                                                                             |
| `excluded`                    | Whether the patch is excluded by default. If `true`, the patch must never be included by default.                                                                                     |
| `deprecated`                  | Whether the patch is deprecated.                                                                                                                                                      |
| `options`                     | An array of options for this patch.                                                                                                                                                   |
| `options.key`                 | The key of the option.                                                                                                                                                                |
| `options.title`               | The title of the option.                                                                                                                                                              |
| `options.description`         | The description of the option.                                                                                                                                                        |
| `options.required`            | Whether the option is required.                                                                                                                                                       |
| `options.choices?`            | An array of choices of the option. This may be `null` if this option has no choices. The element type of this array may be any type. It can be a `String`, `Int` or something else.   |
| `dependencies`                | An array of dependencies, which are patch names.                                                                                                                                      |
| `compatiblePackages`          | An array of packages compatible with this patch.                                                                                                                                      |
| `compatiblePackages.name`     | The name of the package.                                                                                                                                                              |
| `compatiblePackages.versions` | An array of versions of the package compatible with this patch. If empty, all versions are seemingly compatible.                                                                      |

Example:

```json
[
  {
    "name": "remember-video-quality",
    "description": "Adds the ability to remember the video quality you chose in the video quality flyout.",
    "version": "0.0.1",
    "excluded": false,
    "deprecated": false,
    "options": [],
    "dependencies": [
      "integrations",
      "video-id-hook"
    ],
    "compatiblePackages": [
      {
        "name": "com.google.android.youtube",
        "versions": [
          "17.22.36",
          "17.24.35",
          "17.26.35",
          "17.27.39",
          "17.28.34",
          "17.29.34",
          "17.32.35",
          "17.33.42"
        ]
      }
    ]
  },
  {
    "name": "theme",
    "description": "Enables a custom theme.",
    "version": "0.0.1",
    "excluded": false,
    "deprecated": false,
    "options": [
      {
        "key": "theme",
        "title": "Theme",
        "description": "Select a theme.",
        "required": true,
        "choices": [
          "Amoled"
        ]
      }
    ],
    "dependencies": [
      "locale-config-fix"
    ],
    "compatiblePackages": [
      {
        "name": "com.google.android.youtube",
        "versions": []
      }
    ]
  },
  {
    "name": "custom-branding",
    "description": "Changes the YouTube launcher icon and name to your choice (defaults to ReVanced).",
    "version": "0.0.1",
    "excluded": false,
    "deprecated": false,
    "options": [
      {
        "key": "appName",
        "title": "Application Name",
        "description": "The name of the application it will show on your home screen.",
        "required": true,
        "choices": null
      },
      {
        "key": "appIconPath",
        "title": "Application Icon Path",
        "description": "A path to the icon of the application.",
        "required": false,
        "choices": null
      }
    ],
    "dependencies": [
      "locale-config-fix"
    ],
    "compatiblePackages": [
      {
        "name": "com.google.android.youtube",
        "versions": []
      }
    ]
  }
]
```