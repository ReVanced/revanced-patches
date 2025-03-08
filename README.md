# Dynamic Playback Speed Patch for ReVanced

## Overview
This patch adds dynamic playback speed control to YouTube, allowing users to temporarily change video speed by holding the speed button. The speed changes are multiplicative, providing a more intuitive way to adjust playback speed on the fly.

## Features
- Hold to speed up: Multiplies current speed by configurable factor
- Hold to slow down: Divides current speed by configurable factor
- Automatic speed restoration when released
- Respects YouTube's speed limits (0.0625x - 8x)
- Configurable multipliers in settings

## Installation
1. Add this repository to your ReVanced patches
2. Enable the patch during build
3. Configure multipliers in ReVanced settings

## Configuration
- Speed up multiplier: Factor to multiply speed when holding up (default: 2.0)
- Slow down divider: Factor to divide speed when holding down (default: 2.0)

## Compatibility
- YouTube versions: 18.32.39 - 18.35.36
- ReVanced: v2.168.0+
- Android: 8.0+

## License
This patch is licensed under the GPLv3 License. See the LICENSE file for details.

