package it.unipi.mobile.vineplanthealthapp.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import it.unipi.mobile.vineplanthealthapp.R
import it.unipi.mobile.vineplanthealthapp.ui.gallery.Image
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainUtils{

    fun saveImage(contentResolver: ContentResolver, imageUri: Uri, latitude: Double, longitude: Double){
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/VinePlantApp")
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
            Log.d("Image Uri when saved",uri.toString())
            val file = File(getRealPathFromURI(contentResolver, uri))
            val exifInterface = ExifInterface(file.absolutePath)

            // Set GPS location
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToExifFormat(latitude))
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (latitude >= 0) "N" else "S")
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToExifFormat(longitude))
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (longitude >= 0) "E" else "W")

            //set timestamp
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
            val dateTimeString = dateFormat.format(Date(timestamp))
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, dateTimeString)

            //set default plant status
            exifInterface.setAttribute(R.string.plant_status_tag.toString(), R.string.plant_status_not_classified.toString())

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
                try {
                    images.add(Image(bitmap, uri, file.name))
                }
                catch (e: Exception) {
                    Log.e("Image Add Error", e.printStackTrace().toString())
                    continue;
                }
            }
        }
        return images
    }
}