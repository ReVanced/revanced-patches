## 🧩 Patches

The official Patch bundle provided by ReVanced and the community.

> Looking for the JSON variant of this? [Click here](patches.json).

{{ table }}

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