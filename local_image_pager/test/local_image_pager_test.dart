import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:local_image_pager/local_image_pager.dart';

void main() {
  const MethodChannel channel = MethodChannel('local_image_pager');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await LocalImagePager.totalNumber, '42');
  });
}
