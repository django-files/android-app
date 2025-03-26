[![GitHub Release Version](https://img.shields.io/github/v/release/django-files/android-app?logo=github)](https://github.com/django-files/android-app/releases/latest)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=django-files_android-app&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=django-files_android-app)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/django-files/android-app?logo=github&label=updated)](https://github.com/django-files/android-app/graphs/commit-activity)
[![GitHub Top Language](https://img.shields.io/github/languages/top/django-files/android-app?logo=htmx)](https://github.com/django-files/android-app)
[![GitHub repo size](https://img.shields.io/github/repo-size/django-files/android-app?logo=bookstack&logoColor=white&label=repo%20size)](https://github.com/django-files/android-app)
[![GitHub Discussions](https://img.shields.io/github/discussions/django-files/android-app)](https://github.com/django-files/android-app/discussions)
[![GitHub Forks](https://img.shields.io/github/forks/django-files/android-app?style=flat&logo=github)](https://github.com/django-files/android-app/forks)
[![GitHub Repo Stars](https://img.shields.io/github/stars/django-files/android-app?style=flat&logo=github)](https://github.com/django-files/android-app/stargazers)
[![GitHub Org Stars](https://img.shields.io/github/stars/django-files?style=flat&logo=github&label=org%20stars)](https://django-files.github.io/)
[![Discord](https://img.shields.io/discord/899171661457293343?logo=discord&logoColor=white&label=discord&color=7289da)](https://discord.gg/wXy6m2X8wY)

# Django Files Android Application

- [Install](#Install)
- [Support](#Support)
- [Contributing](#Contributing)
- [Development](#Development)

Coming Soon...

| Resources | Links                                        |
| --------- | -------------------------------------------- |
| Website   | https://django-files.github.io/              |
| GitHub    | https://github.com/django-files              |
| Server    | https://github.com/django-files/django-files |
| iOS App   | https://github.com/django-files/ios-client/  |

# Install

Until the app is published it must be loaded with [ADB](https://developer.android.com/tools/adb).
This requires using the command line interface or Android Studio.

1. Download and Install the Android SDK Platform Tools or Android Studio.

   - https://developer.android.com/tools/releases/platform-tools#downloads
   - https://developer.android.com/studio

2. Ensure that usb or wifi debugging is enabled in the Android developer settings and verify.

```shell
$ adb devices
List of devices attached
RF9M33Z1Q0M     device
```

3. Download or build a debug apk (use gradle or Android Studio).

   - https://github.com/django-files/android-app/releases

4. Unzip the release and change into the directory.
   If you built a release it should be in `app/build/outputs/apk/debug`.
5. Then install the apk to your device with adb.

```shell
$ adb -s RF9M33Z1Q0M install .\app-debug.apk

Performing Streamed Install
Success
```

For more details see the [ADB Docs](https://developer.android.com/tools/adb#move).

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

# Development

Android Studio: https://developer.android.com/studio

For now see [Install](#Install).
