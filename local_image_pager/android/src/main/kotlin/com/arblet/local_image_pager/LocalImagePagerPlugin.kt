package com.arblet.local_image_pager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi

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


//  private fun fetchPhotos(start: Int?, end: Int?, @NonNull result: Result) {
//    if (start == null || end == null) {
//      Log.e(tag, "getLatestImages:param error. start or end is null.");
//      result.error(error_code_param, "getLatestImages:param error. start or end is null.", null)
//    }
//
//    try {
//      val columns = arrayOf(MediaStore.Images.ImageColumns.DATE_ADDED, MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
//      val limit = end!! - start!! + 1;
//      val orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + start
//
//      val photos: MutableList<String> = mutableListOf()
//
//      context.contentResolver.query(
//              MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//              columns,
//              null,
//              null,
//              orderBy
//      )?.use { cursor ->
//        while (cursor.moveToNext()) {
//          val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
//          photos.add(cursor.getString(idx))
//        }
//        cursor.close()
//      }
//      result.success(photos);
//    } catch (e: Throwable) {
//      Log.e(tag, e.toString())
//      result.error(error_code_catch_exception, e.toString(), null)
//    }
//  }



  private fun fetchPhotos(
          start: Int?, end: Int?,
//          context: Context,
//          orderBy: String,
//          orderAscending: Boolean,
//          limit: Int = 20,
//          offset: Int = 0,
          @NonNull result: Result
  ) {
    if (start == null || end == null) {
      Log.e(tag, "getLatestImages:param error. start or end is null.");
      result.error(error_code_param, "getLatestImages:param error. start or end is null.", null)
    }

    val photos: MutableList<String> = mutableListOf()

    val limit = end!! - start!! + 1;
    val orderBy = MediaStore.Images.Media.DATE_ADDED

    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED
//            MediaStore.Files.FileColumns.MEDIA_TYPE
//            MediaStore.Files.FileColumns.MIME_TYPE,
//            MediaStore.Files.FileColumns.TITLE
    )
    val whereCondition = "${MediaStore.Files.FileColumns.MEDIA_TYPE}"
    val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()
//            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )

    createCursor(
            contentResolver = context.contentResolver,
            collection = collection,
            projection = projection,
            whereCondition = whereCondition,
            selectionArgs = selectionArgs,
            orderBy = orderBy,
            orderAscending = false,
            limit = limit,
            offset = start
    )?.use { cursor ->
      while (cursor.moveToNext()) {
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        photos.add(cursor.getString(idx))
      Log.d(tag, "path:$idx:${cursor.getString(idx)}")
      }
      cursor.close()
    }

    result.success(photos);
  }

  private fun createCursor(
          contentResolver: ContentResolver,
          collection: Uri,
          projection: Array<String>,
          whereCondition: String,
          selectionArgs: Array<String>,
          orderBy: String,
          orderAscending: Boolean,
          limit: Int = 20,
          offset: Int = 0
  ): Cursor? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
      val selection = createSelectionBundle(whereCondition, selectionArgs, orderBy, orderAscending, limit, offset)
      contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, null)
    }
    else -> {
      val orderDirection = if (orderAscending) "ASC" else "DESC"
      var order = when (orderBy) {
        "ALPHABET" -> "${MediaStore.Audio.Media.TITLE}, ${MediaStore.Audio.Media.ARTIST} $orderDirection"
        else -> "${MediaStore.Audio.Media.DATE_ADDED} $orderDirection"
      }
      order += " LIMIT $limit OFFSET $offset"
      contentResolver.query(collection, projection, whereCondition, selectionArgs, order)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun createSelectionBundle(
          whereCondition: String,
          selectionArgs: Array<String>,
          orderBy: String,
          orderAscending: Boolean,
          limit: Int,
          offset: Int
  ): Bundle = Bundle().apply {
    putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
    putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
    when (orderBy) {
      "ALPHABET" -> putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Files.FileColumns.TITLE))
      else -> putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Files.FileColumns.DATE_ADDED))
    }
    // Sorting direction
    val orderDirection =
            if (orderAscending) ContentResolver.QUERY_SORT_DIRECTION_ASCENDING else ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
    putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, orderDirection)
    // Selection
//    putString(ContentResolver.QUERY_ARG_SQL_SELECTION, whereCondition)
//    putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
  }


  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
