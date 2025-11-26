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

    // Nuevas imágenes
    private lateinit var imgHeatmapRetorno: ImageView
    private lateinit var imgBoxRecurrentes: ImageView
    private lateinit var imgAciertosErroresRetorno: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.python_activity)

        btnRunPython = findViewById(R.id.btnRunPython)
        tvOutput = findViewById(R.id.tvOutput)

        imgHistograma = findViewById(R.id.imgHistograma)
        imgScatter = findViewById(R.id.imgScatter)
        imgPuntuacion = findViewById(R.id.imgPuntuacion)

        // Nuevos
        imgHeatmapRetorno = findViewById(R.id.imgHeatmapRetorno)
        imgBoxRecurrentes = findViewById(R.id.imgBoxRecurrentes)
        imgAciertosErroresRetorno = findViewById(R.id.imgAciertosErroresRetorno)

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

            val result = module.callAttr("run", filesDir.absolutePath)
            tvOutput.text = "Script ejecutado correctamente: $result"

            // Imágenes ya existentes
            loadImage("hist_session_length.png", imgHistograma)
            loadImage("scatter_errors_vs_aciertos.png", imgScatter)
            loadImage("bar_puntuacion_dificultad.png", imgPuntuacion)

            // Nuevas imágenes
            loadImage("heatmap_probabilidad_retorno.png", imgHeatmapRetorno)
            loadImage("box_gameTime_recurrentes.png", imgBoxRecurrentes)
            loadImage("bar_aciertos_errores_recurrentes.png", imgAciertosErroresRetorno)

        } catch (e: Exception) {
            tvOutput.text = "Error ejecutando Python: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun loadImage(filename: String, imageView: ImageView) {
        val file = File(filesDir, filename)
        if (file.exists()) {
            val drawable = Drawable.createFromPath(file.absolutePath)
            imageView.setImageDrawable(drawable)
        } else {
            imageView.setImageDrawable(null)
            tvOutput.append("\nNo se encontró: $filename")
        }
    }
}