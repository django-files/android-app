[![GitHub Release Version](https://img.shields.io/github/v/release/django-files/android-app?logo=github)](https://github.com/django-files/android-app/releases/latest)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/django-files/android-app?logo=github&label=updated)](https://github.com/django-files/android-app/graphs/commit-activity)
[![GitHub Top Language](https://img.shields.io/github/languages/top/django-files/android-app?logo=htmx)](https://github.com/django-files/android-app)
[![GitHub Repo Size](https://img.shields.io/github/repo-size/django-files/android-app?logo=bookstack&logoColor=white&label=repo%20size)](https://github.com/django-files/android-app)
[![GitHub Discussions](https://img.shields.io/github/discussions/django-files/android-app)](https://github.com/django-files/android-app/discussions)
[![GitHub Forks](https://img.shields.io/github/forks/django-files/android-app?style=flat&logo=github)](https://github.com/django-files/android-app/forks)
[![GitHub Repo Stars](https://img.shields.io/github/stars/django-files/android-app?style=flat&logo=github)](https://github.com/django-files/android-app/stargazers)
[![GitHub Org Stars](https://img.shields.io/github/stars/django-files?style=flat&logo=github&label=org%20stars)](https://django-files.github.io/)
[![Discord](https://img.shields.io/discord/899171661457293343?logo=discord&logoColor=white&label=discord&color=7289da)](https://discord.gg/wXy6m2X8wY)

# Django Files Android App

- [Install](#Install)
  - [Setup](#Setup)
- [Features](#Features)
  - [Planned](#Planned)
- [Development](#Development)
- [Support](#Support)
- [Contributing](#Contributing)

Allows you to Share or Open any file with your Django Files server.
The URL to the file is automatically copied to the clipboard and the preview is shown in the app.

| Resource | Resource&nbsp;Link                           |
| -------- | :------------------------------------------- |
| Website  | https://django-files.github.io/              |
| GitHub   | https://github.com/django-files              |
| Server   | https://github.com/django-files/django-files |
| iOS App  | https://github.com/django-files/ios-client   |

## Install

Until the app is published it must be loaded with [ADB](https://developer.android.com/tools/adb).  
This requires using Android Studio or the command line interface.

1. Download and Install Android Studio or the Android SDK Platform Tools.

https://developer.android.com/studio  
https://developer.android.com/tools/releases/platform-tools#downloads

2. Ensure that usb or wifi debugging is enabled in the Android developer settings and verify.

If using **Android Studio** your device should show up! Simply import the project, and press Play ▶️

Otherwise, continue with the steps below...

This assumes that `adb` is in your PATH. List and verify the device is connected with:

```shell
$ adb devices

List of devices attached
RF9M33Z1Q0M     device
```

3. Download or build a debug apk (use gradle or Android Studio).

https://github.com/django-files/android-app/releases

4. Unzip the release and change into the directory.

The built apk should be here: `app/build/outputs/apk/debug`

5. Then install the apk to your device with adb.

```shell
$ adb -s RF9M33Z1Q0M install .\app-debug.apk

Performing Streamed Install
Success
```

For more details, see the [ADB Documentation](https://developer.android.com/tools/adb#move).

### Setup

1. [Install](#Install) and open the app.
2. Enter the URL to your Django Files server.
3. Log in as you normally would on the website.
4. Done! You can now share any file to your Django Files server...

## Features

- Share or Open any file and automatically copy the URL to the clipboard.
- Ability to manually change servers by entering a new URL from the Server List menu.

### Planned

- Ability to save multiple servers and switch between them automatically in the Server List menu.
- Ability for the app to log you in if your session is expired or when switching servers.

# Development

Android Studio: https://developer.android.com/studio

For now see [Install](#Install).

# Support

For general help or to request a feature, see:

- Q&A Discussion: https://github.com/django-files/android-app/discussions/categories/q-a
- Request a Feature: https://github.com/django-files/android-app/discussions/categories/feature-requests

If you are experiencing an issue/bug or getting unexpected results, you can:

- Report an Issue: https://github.com/django-files/android-app/issues
- Chat with us on Discord: https://discord.gg/wXy6m2X8wY
- Provide General Feedback: [https://cssnr.github.io/feedback/](https://cssnr.github.io/feedback/?app=Django%20Files%20Android%20App)

# Contributing

Currently, the best way to contribute to this project is to star this project on GitHub.

You can also support other related projects:

- [Django Files Server](https://github.com/django-files/django-files)
- [Django Files iOS App](https://github.com/django-files/ios-client)
- [Django Files Android App](https://github.com/django-files/django-files)
