import 'dart:async';

import 'package:flutter/services.dart';
import 'package:synchronized/synchronized.dart' as synchronized;

/// A plugin for showing your device's local images with pagination.
class LocalImagePager {
  static const MethodChannel _channel =
  const MethodChannel('local_image_pager');

  final _lock = synchronized.Lock();

  /// The total number of images in your device.
  static Future<int> get totalNumber async {
    return await _channel.invokeMethod('getTotalNumber');
  }

  /// Fetches the local images from [start] index to [end] index, order by date desc.
  Future<List<String>> latestImages(int start, int end) =>
      _lock.synchronized(() async {
        final photos = await _channel
            .invokeMethod('getLatestImages', {'start': start, 'end': end});
        return photos.cast<String>();
      });
}
