package it.unipi.mobile.vineplanthealthapp.utils

import android.net.Uri
import android.provider.MediaStore
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Environment
import it.unipi.mobile.vineplanthealthapp.ui.gallery.Image
import java.io.File


class MainUtils {

    fun saveImage(contentResolver: ContentResolver, imageUri: Uri, latitude: Double, longitude: Double){
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "new_image.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val inputStream = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.let { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            }

            val file = File(getRealPathFromURI(contentResolver, uri))
            val exifInterface = ExifInterface(file.absolutePath)
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToExifFormat(latitude))
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (latitude >= 0) "N" else "S")
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToExifFormat(longitude))
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (longitude >= 0) "E" else "W")
            exifInterface.saveAttributes()
        }
    }

    private fun getRealPathFromURI(contentResolver: ContentResolver, contentUri: Uri): String {
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            return it.getString(idx)
        }
        return ""
    }

    private fun convertToExifFormat(degrees: Double): String {
        val degree = Math.abs(degrees).toInt()
        val minute = ((Math.abs(degrees) - degree) * 60).toInt()
        val second = ((Math.abs(degrees) - degree - minute / 60.0) * 3600).toInt()

        return "$degree/1,$minute/1,$second/1"
    }
    public fun createArrayImages( imageFiles: Array<File>): MutableList<Image> {
        val images = mutableListOf<Image>()
        for (file in imageFiles) {
            if (file.isFile && (file.path.endsWith(".jpg") || file.path.endsWith(".png"))) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val uri = Uri.fromFile(file)
                images.add(Image(bitmap, uri, file.name))
            }
        }
        return images
    }
}