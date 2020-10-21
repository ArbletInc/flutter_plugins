import 'dart:async';
import 'dart:io';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:local_image_pager/local_image_pager.dart';

/// An example app for local_image_pager plugin.
void main() {
  runApp(MaterialApp(
    title: 'Local images pager example',
    home: LocalImagePagerDemo(),
  ));
}

class LocalImagePagerDemo extends StatefulWidget {
  @override
  _LocalImagePagerDemoState createState() => _LocalImagePagerDemoState();
}

/// How many images you want to fetch per 1 page.
const per_load_count = 30;

/// GridView's crossAxisCount.
const cross_axis_count = 4;

/// GridView's mainAxisSpacing, crossAxisSpacing, and padding. (Used to calculate the thumbnail size.)
const double spacing = 5;

/// GridView's padding. (Used to calculate the thumbnail size.)
const double horizontal_padding = 10;

class _LocalImagePagerDemoState extends State<LocalImagePagerDemo> {
  /// You need to ask [Permission.storage].
  final _permissions = [Permission.storage];
  final _completers = <Completer>[];

  @override
  void initState() {
    super.initState();
  }

  Future<bool> get _initPermissions async {
    final statuses = (await _permissions.request()).values;
    for (final status in statuses) {
      if (status != PermissionStatus.granted) {
        print('_initPermissions:not granted');
        return false;
      }
    }
    print('_initPermissions:granted');
    return true;
  }

  @override
  Widget build(BuildContext context) {
    // Calculate the thumbnail's size first to improve performance.
    final itemSize = ((MediaQuery.of(context).size.width -
        spacing * (cross_axis_count - 1) - horizontal_padding * 2) /
        cross_axis_count);

    final cacheSize = (itemSize * 1.5).round();

    final pager = LocalImagePager();

    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Local images pager'),
          ),
          body: Column(children: [
            const SizedBox(height: 100,
                child: Center(child: Text('Your own widget here.', style: TextStyle(fontSize: 20, color: Colors.blue, fontWeight: FontWeight.bold),),)),
            Expanded(child: FutureBuilder(
              future: _initPermissions,
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  if (snapshot.data) {
                    return FutureBuilder(
                      future: LocalImagePager.totalNumber,
                      builder: (context, snapshot) {
                        if (snapshot.hasData) {
                          final count = snapshot.data;
                          if (count == 0) {
                            return const Center(
                              child: Text('No images'),
                            );
                          } else {
                            return GridView.builder(
                                padding: const EdgeInsets.symmetric(horizontal: horizontal_padding),
                                gridDelegate:
                                const SliverGridDelegateWithFixedCrossAxisCount(
                                  crossAxisCount: cross_axis_count,
                                  mainAxisSpacing: spacing,
                                  crossAxisSpacing: spacing,
                                ),
                                itemCount: count,
                                itemBuilder: (context, index) =>
                                    _itemBuilder(
                                        pager, count, index, itemSize, cacheSize));
                          }
                        } else if (snapshot.hasError) {
                          return Center(
                            child: Text(
                              snapshot.error.toString(),
                              style: const TextStyle(color: Colors.red),
                            ),
                          );
                        } else {
                          return const Center(
                              child: CircularProgressIndicator());
                        }
                      },
                    );
                  } else {
                    return Column(
                      children: [
                        const Text(
                          'Permission not granted, try again.',
                          style: const TextStyle(color: Colors.red),
                        ),
                        FlatButton(
                            onPressed: () {
                              setState(() {});
                            },
                            child: const Text('OK'))
                      ],
                    );
                  }
                } else if (snapshot.hasError) {
                  return Center(
                    child: Text(
                      snapshot.error.toString(),
                      style: const TextStyle(color: Colors.red),
                    ),
                  );
                } else {
                  return const Center(child: CircularProgressIndicator());
                }
              },
            ),)
          ])),
    );
  }

  Widget _itemBuilder(
      LocalImagePager paginate, int totalNumber, int index, double itemSize, int cacheSize) {
    _load(paginate, totalNumber, index);

    return FutureBuilder(
      future: _completers[index].future,
      builder: (context, snapshot) {
        if (snapshot.hasData) {
          return Image.file(
            File(snapshot.data),
            fit: BoxFit.cover,
            height: itemSize,
            cacheHeight: cacheSize,
          );
        } else if (snapshot.hasError) {
          print('_itemBuilder:error:${snapshot.error.toString()}');
          return const SizedBox(
            width: 0,
            height: 0,
          );
        } else {
          return const SizedBox(
            width: 0,
            height: 0,
          );
        }
      },
    );
  }

  _load(LocalImagePager paginate, int count, int itemIndex) async {
    if (itemIndex % per_load_count != 0) {
      return;
    }

    if (itemIndex >= _completers.length) {
      final toLoad = min(count - itemIndex, per_load_count);
      if (toLoad > 0) {
        _completers.addAll(List.generate(toLoad, (index) {
          return Completer();
        }));

        try {
          final images =
          await paginate.latestImages(itemIndex, itemIndex + toLoad - 1);
          images.asMap().forEach((index, item) {
            _completers[itemIndex + index].complete(item);
          });
        } catch (e) {
          print('_load:error:${e.toString()}');
          _completers
              .sublist(itemIndex, itemIndex + toLoad)
              .forEach((completer) {
            completer.completeError(e);
          });
        }
      }
    }
  }
}
