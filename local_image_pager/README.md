**local_image_pager**

A plugin for showing your device's local images with pagination.

![SVID_20201019_170821_1 mp4_1280_606_606_1280_606_1280](https://user-images.githubusercontent.com/17906677/96701682-d6ebd280-13cb-11eb-98f3-1ef91e761d47.gif)

## Features

* Get total number of images in your device.
* Fetch local images with pagination.

## Getting started

In the `pubspec.yaml` of your flutter project, add the following dependency:

```yaml
dependencies:
  ...
  local_image_pager:
```

In your library add the following import:

```dart
import 'package:local_image_pager/local_image_pager.dart';
```

For help getting started with Flutter, view the online [documentation](https://flutter.io/).


## Permissions

Applications using this plugin require the following user permissions. 
### iOS

Add the following key to your _Info.plist_ file, located in `<project root>/ios/Runner/Info.plist`:

* `NSPhotoLibraryUsageDescription` - describe why your app needs permission for the photo library. This is called _Privacy - Photo Library Usage Description_ in the visual editor. This permission is required for the app to read the image and album information. 

### Android

Add the storage permission to your _AndroidManifest.xml_ file, located in `<project root>/android/app/src/main/AndroidManifest.xml`:

* `android.permission.READ_EXTERNAL_STORAGE` - this allows the app to query and read the image and album information.
