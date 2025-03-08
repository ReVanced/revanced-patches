# Dynamic Playback Speed ▶️
[![build](https://github.com/anonymousik/Dynamic-Playback-Speed-Patch-for-ReVanced/actions/workflows/build.yml/badge.svg)](https://github.com/anonymousik/Dynamic-Playback-Speed-Patch-for-ReVanced/actions/workflows/build.yml)
[![license: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Dynamic playback speed control patch for ReVanced.

## 🚀 Features

This patch adds the following features:

* Dynamic speed control through long-press gesture
* Multiplicative speed changes based on current speed
* Configurable speed up/down multipliers
* Automatic speed restoration on release
* Seamless integration with YouTube's native speed controls
* Compatible with existing playback speed menu

## ⚡️ Performance

The patch is designed for minimal performance impact:

* No background services
* Efficient speed calculations
* Native API integration
* Minimal memory footprint

## 📋 Prerequisites

* ReVanced Manager or CLI v2.168.0 or higher
* YouTube v18.32.39 - v18.35.36
* Android 8.0+

## 💻 Installation

### Using ReVanced Manager

1. Open ReVanced Manager
2. Select YouTube from the apps list
3. Choose "Dynamic Playback Speed" from available patches
4. Patch and install

### Using ReVanced CLI

```bash
java -jar revanced-cli.jar \
    -a youtube.apk \
    -c \
    -o youtube-revanced.apk \
    -b revanced-patches.jar \
    -i dynamic-playback-speed
```

## ⚙️ Configuration

The following settings can be configured in ReVanced settings:

| Setting | Description | Default |
|---------|-------------|---------|
| Speed Up Multiplier | Factor to multiply speed when holding up | 2.0 |
| Slow Down Divider | Factor to divide speed when holding down | 2.0 |
| Enable/Disable | Toggle the feature | Enabled |

## 📝 Usage

1. Play any video in YouTube
2. Long press the speed button:
   * Press and hold up to increase speed
   * Press and hold down to decrease speed
3. Release to return to normal speed

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🐛 Known Issues

* Speed changes might be delayed on devices with low processing power
* May not work with some custom YouTube modifications
* Limited to YouTube's maximum speed of 8x

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 📬 Contact

Project Link: [https://github.com/anonymousik/Dynamic-Playback-Speed-Patch-for-ReVanced](https://github.com/anonymousik/Dynamic-Playback-Speed-Patch-for-ReVanced)

## 🙏 Acknowledgments

* [ReVanced Team](https://github.com/revanced) for the amazing platform
* YouTube API documentation
* All contributors who helped with testing and improvements

## 📊 Compatibility

| App | Versions |
|-----|----------|
| YouTube | 18.32.39 - 18.35.36 |
| ReVanced | v2.168.0+ |
| Android | 8.0+ |

## 📝 Changelog

### v0.0.1
* Initial release
* Basic speed control functionality
* Configuration options
* YouTube integration

## 🔄 Integration

This patch integrates with:
* YouTube's native speed controls
* ReVanced settings menu
* Other playback-related patches

## ⚠️ Disclaimer

This project is not affiliated with YouTube or Google. This is an independent patch created for educational purposes.