package com.example.myapplication

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File

class PythonActivity : AppCompatActivity() {

    private lateinit var btnRunPython: Button
    private lateinit var tvOutput: TextView
    private lateinit var imgHistograma: ImageView
    private lateinit var imgScatter: ImageView
    private lateinit var imgPuntuacion: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.python_activity)

        // Inicializar vistas
        btnRunPython = findViewById(R.id.btnRunPython)
        tvOutput = findViewById(R.id.tvOutput)
        imgHistograma = findViewById(R.id.imgHistograma)
        imgScatter = findViewById(R.id.imgScatter)
        imgPuntuacion = findViewById(R.id.imgPuntuacion)

        // Inicializar Python si no está iniciado
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        btnRunPython.setOnClickListener {
            runPythonAnalysis()
        }
    }

    private fun runPythonAnalysis() {
        try {
            val py = Python.getInstance()
            val module = py.getModule("game_analysis")

            // Pasar filesDir.absolutePath al script para que lea game_data.json y genere PNGs
            val result = module.callAttr("run", filesDir.absolutePath)
            tvOutput.text = "Script ejecutado correctamente: $result"

            // Cargar imágenes generadas por Python desde filesDir
            loadImage("hist_session_length.png", imgHistograma)
            loadImage("scatter_errors_vs_aciertos.png", imgScatter)
            loadImage("bar_puntuacion_dificultad.png", imgPuntuacion)

        } catch (e: Exception) {
            tvOutput.text = "Error ejecutando Python: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun loadImage(filename: String, imageView: ImageView) {
        val file = File(filesDir, filename)
        if (file.exists()) {
            val drawable: Drawable? = Drawable.createFromPath(file.absolutePath)
            imageView.setImageDrawable(drawable)
        } else {
            imageView.setImageDrawable(null)
            tvOutput.append("\nNo se encontró: $filename")
        }
    }
}
