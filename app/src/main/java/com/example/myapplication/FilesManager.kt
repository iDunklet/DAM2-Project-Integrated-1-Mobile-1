package com.example.myapplication

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

//Comentario

class FilesManager {
    companion object {

        private const val FOLDER = "json"
        private const val FILE_NAME = "ColorsGame.json"

        fun readFile(context: Context): MutableList<Jugador> {
            val dir = File(context.filesDir, FOLDER)
            val file = File(dir, FILE_NAME)

            if (!file.exists()) return mutableListOf()

            return try {
                FileReader(file).use { reader ->
                    val type = object : TypeToken<MutableList<Jugador>>() {}.type
                    Gson().fromJson(reader, type)
                }
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        fun saveFile(context: Context, jugadores: List<Jugador>) {
            val dir = File(context.filesDir, FOLDER)

            // Create directory if it doesn't exist
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, FILE_NAME)

            FileWriter(file).use { writer ->
                val json = Gson().toJson(jugadores)
                writer.write(json)
            }
        }
    }
}
