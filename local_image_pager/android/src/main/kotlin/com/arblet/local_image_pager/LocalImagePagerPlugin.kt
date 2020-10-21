package com.arblet.local_image_pager

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** LocalImagePagerPlugin */
class LocalImagePagerPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var context: Context
  private lateinit var channel: MethodChannel
  private val tag = "LocalImagePagerPlugin";
  private val error_code_param = "-1"
  private val error_code_catch_exception = "-2"

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext

    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "local_image_pager")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getTotalNumber") {
      totalNumber(result)
    } else if (call.method == "getLatestImages") {
      fetchPhotos(call.argument("start") as Int?, call.argument("end") as Int?, result)
    } else {
      result.notImplemented()
    }
  }

  private fun totalNumber(@NonNull result: Result) {
    try {
      var count = 0
      val columns = arrayOf(MediaStore.Images.ImageColumns.DATE_ADDED)
      val cursor = context.contentResolver.query(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, null)
      if (cursor != null) {
        count = cursor.count
      }
      cursor!!.close()
      result.success(count)
    } catch (e: Throwable) {
      Log.e(tag, e.toString())
      result.error(error_code_catch_exception, e.toString(), null)
    }
  }


  private fun fetchPhotos(start: Int?, end: Int?, @NonNull result: Result) {
    if (start == null || end == null) {
      Log.e(tag, "getLatestImages:param error. start or end is null.");
      result.error(error_code_param, "getLatestImages:param error. start or end is null.", null)
    }

    try {
      val columns = arrayOf(MediaStore.Images.ImageColumns.DATE_ADDED, MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
      val limit = end!! - start!! + 1;
      val orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + start

      val photos: MutableList<String> = mutableListOf()

      context.contentResolver.query(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
              columns,
              null,
              null,
              orderBy
      )?.use { cursor ->
        while (cursor.moveToNext()) {
          val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
          photos.add(cursor.getString(idx))
        }
        cursor.close()
      }
      result.success(photos);
    } catch (e: Throwable) {
      Log.e(tag, e.toString())
      result.error(error_code_catch_exception, e.toString(), null)
    }
  }


  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
