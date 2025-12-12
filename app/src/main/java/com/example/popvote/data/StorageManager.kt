package com.example.popvote.data

import android.content.Context
import android.net.Uri
import com.example.popvote.model.AppData
import com.example.popvote.model.Film
import com.example.popvote.model.Folder
import com.example.popvote.model.Wish
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

class StorageManager(private val context: Context) {
    private val gson = Gson()
    private val fileName = "popvote_data.json"
    // saving datas

    fun saveAll(folders: List<Folder>, allFilms: List<Film>,wishlist:List<Wish>) {
        val appData = AppData(folders, allFilms, wishlist)
        val jsonString = gson.toJson(appData)

        //writing the file in the  private memory of the app
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }


    fun loadAll(): AppData {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return AppData(emptyList(), emptyList(), emptyList())

        return try {
            val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<AppData>() {}.type
            gson.fromJson<AppData>(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
            AppData(emptyList(), emptyList(), emptyList())
        }
    }


    // Saving Images
    //takes the URI from the gallery, create a new address with the same datas,use the new
    // address to paste the image, delete the temporary URI
    fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)

            val outputStream = file.outputStream()
            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}