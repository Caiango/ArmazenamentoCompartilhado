package com.josecmj.armazenamentocompartilhado

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import java.io.File

class DataSource(val context: Context) {
    private val filesLiveData =  getImages()

    fun getFiles(): MutableLiveData<List<MyImage>> {
        return MutableLiveData(filesLiveData)
    }


    companion object {
        private var INSTANCE: DataSource? = null

        fun getDataSource(context: Context): DataSource {
            return synchronized(DataSource::class) {
                val newInstance = INSTANCE ?: DataSource(context)
                INSTANCE = newInstance
                newInstance
            }
        }
    }

    fun getImages(): List<MyImage> {
        val list = mutableListOf<MyImage>()
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DISPLAY_NAME
        )

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        ).use {
            it?.let { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Images.ImageColumns._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Images.ImageColumns.DISPLAY_NAME)

                while(cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    list.add(MyImage(id, displayName, contentUri))
                }
            }
        }
        return list
    }

    private fun save(name: String, bucketName: String, bmp: Bitmap){
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$bucketName/")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUri = context.contentResolver.insert(collection, values)

        if (imageUri != null) {
            context.contentResolver.openOutputStream(imageUri).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(imageUri, values, null, null)
        }

    }


}