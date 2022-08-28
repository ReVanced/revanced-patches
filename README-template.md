## 🧩 Patches

The official Patch bundle provided by ReVanced and the community.

> Looking for the JSON variant of this? [Click here](patches.json).

{{ table }}

## 📝 JSON Format

This section explains the JSON format for the [patches.json](patches.json) file.

The file contains an array of objects, each object representing a patch. The object contains the following properties:

| key                           | description                                                                                                      |
|-------------------------------|------------------------------------------------------------------------------------------------------------------|
| `name`                        | The name of the patch.                                                                                           |
| `description`                 | The description of the patch.                                                                                    |
| `version`                     | The version of the patch.                                                                                        |
| `excluded`                    | Whether a patch is excluded by default. If `true`, the patch must never be included by default.                  |
| `dependencies`                | An array of dependencies, which are patch names.                                                                 |
| `compatiblePackages`          | An array of packages compatible with this patch.                                                                 |
| `compatiblePackages.name`     | The name of the package.                                                                                         |
| `compatiblePackages.versions` | An array of versions of the package compatible with this patch. If empty, all versions are seemingly compatible. |

Example:

```json
[
  {
    "name": "remember-video-quality",
    "description": "Adds the ability to remember the video quality you chose in the video quality flyout.",
    "version": "0.0.1",
    "excluded": false,
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
          "17.32.35"
        ]
      }
    ]
  },
  {
    "name": "client-spoof",
    "description": "Spoofs the YouTube or Vanced client to prevent playback issues.",
    "version": "0.0.1",
    "excluded": false,
    "dependencies": [],
    "compatiblePackages": [
      {
        "name": "com.google.android.youtube",
        "versions": []
      },
      {
        "name": "com.vanced.android.youtube",
        "versions": []
      }
    ]
  }
]
```