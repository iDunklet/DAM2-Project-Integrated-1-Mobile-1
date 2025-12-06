package com.example.myapplication

import UserGameData
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Date

class RegisterActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    private var numeroRondas: Int = 0
    private var nivelDificultad: Int = 0
    private val TARGET_ROUNDS = 1
    private val TARGET_DIFFICULTY = 2


    private lateinit var btnRondas5: Button
    private lateinit var btnRondas10: Button
    private lateinit var btnRondas15: Button
    private lateinit var botonesRondas: List<Button>

    private lateinit var btnNivel1: Button
    private lateinit var btnNivel2: Button
    private lateinit var btnNivel3: Button
    private lateinit var botonesNivel: List<Button>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_activity)

        mediaPlayer = MediaPlayer.create(this, R.raw.school)
        mediaPlayer.start()

        // Inicialización de Vistas
        val newPlayerName = findViewById<EditText>(R.id.TextboxNombre)
        val newPlayerAge = findViewById<EditText>(R.id.TextboxEdad)
        val btnAceptar = findViewById<Button>(R.id.buttonRegisterAceptar)


        btnRondas5 = findViewById(R.id.buttonRondas5)
        btnRondas10 = findViewById(R.id.buttonRondas10)
        btnRondas15 = findViewById(R.id.buttonRondas15)
        botonesRondas = listOf(btnRondas5, btnRondas10, btnRondas15)


        btnNivel1 = findViewById(R.id.buttonNivel1)
        btnNivel2 = findViewById(R.id.buttonNivel2)
        btnNivel3 = findViewById(R.id.buttonNivel3)
        botonesNivel = listOf(btnNivel1, btnNivel2,btnNivel3)


        // Listeners RONDAS
        btnRondas5.setOnClickListener {
            handleRoundSelection(it as Button, 5)
        }
        btnRondas10.setOnClickListener {
            handleRoundSelection(it as Button, 10)
        }
        btnRondas15.setOnClickListener {
            handleRoundSelection(it as Button, 15)
        }

        // Listeners NIVEL
        btnNivel1.setOnClickListener {
            handleLevelSelection(it as Button, 1)
        }
        btnNivel2.setOnClickListener {
            handleLevelSelection(it as Button, 2)
        }
        btnNivel3.setOnClickListener {
            handleLevelSelection(it as Button, 3)
        }


        btnAceptar.setOnClickListener {
            if (validateInputs(newPlayerName, newPlayerAge)) {


                val nuevaPartida = UserGameData(
                    rondas = numeroRondas,
                    dificultad = nivelDificultad,
                    aciertos = 0,
                    errores = 0,
                    fechaHoraInicio = Date(),
                    fechaHoraFin = null,
                    gameTime = 0
                )
                val nuevoJugador = Jugador(
                    nombre = newPlayerName.text.toString(),
                    edad = newPlayerAge.text.toString().toInt(),
                    partidas = mutableListOf()
                )
                nuevoJugador.partidas.add(nuevaPartida)

                when (nuevaPartida.dificultad) {
                    1, 2 -> {
                        val intent = Intent(this, GameActivity_2::class.java)
                        intent.putExtra("JUGADOR", nuevoJugador)
                        onPause()
                        startActivity(intent)
                    }
                    3 -> {
                        val intent = Intent(this, GameActivity_ds::class.java)
                        intent.putExtra("JUGADOR", nuevoJugador)
                        onPause()
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun handleRoundSelection(boton: Button, rondas: Int) {
        MetodosUniversal.setSelectionStyle(
            botonPresionado = boton,
            valor = rondas,
            botonesGrupo = botonesRondas,
            targetVariable = TARGET_ROUNDS,
            onUpdateData = ::updateDataVariables
                                          )
    }

    private fun handleLevelSelection(boton: Button, nivel: Int) {
        MetodosUniversal.setSelectionStyle(
            botonPresionado = boton,
            valor = nivel,
            botonesGrupo = botonesNivel,
            targetVariable = TARGET_DIFFICULTY,
            onUpdateData = ::updateDataVariables
                                          )
    }

    private fun updateDataVariables(target: Int, value: Int) {
        if (target == TARGET_ROUNDS) {
            numeroRondas = value
            Log.d("RegisterActivity", "Rondas seleccionadas: $numeroRondas")
        } else if (target == TARGET_DIFFICULTY) {
            nivelDificultad = value
            Log.d("RegisterActivity", "Dificultad seleccionada: $nivelDificultad")
        }
    }

    private fun validateInputs(nameEt: EditText, ageEt: EditText): Boolean {
        if (nameEt.text.isNullOrBlank()) {
            nameEt.error = "El nombre es obligatorio."
            Toast.makeText(this, "Falta el nombre.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (ageEt.text.isNullOrBlank() || ageEt.text.toString().toIntOrNull() == null) {
            ageEt.error = "La edad debe ser un número."
            Toast.makeText(this, "Falta o es incorrecta la edad.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (numeroRondas == 0) {
            Toast.makeText(this, "Debes seleccionar el número de rondas.", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (nivelDificultad == 0) {
            Toast.makeText(this, "Debes seleccionar la dificultad.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
    override fun onPause() {
        super.onPause()
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }
}