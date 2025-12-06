package com.example.myapplication

import UserGameData
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var jugador: Jugador
    private lateinit var partida: UserGameData
    private lateinit var btnRePlay: Button
    private lateinit var btnExit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        mediaPlayer = MediaPlayer.create(this, R.raw.funny_toy)
        mediaPlayer.start()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_over)
        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        jugador = (intent.getSerializableExtra("JUGADOR") as? Jugador)!!

        val partidaActual = jugador.partidas.lastOrNull()
        partida = partidaActual!!
        val tvJugador = findViewById<TextView>(R.id.labelJugador)
        val tvAciertos = findViewById<TextView>(R.id.labelAciertos)
        val tvErrores = findViewById<TextView>(R.id.labelErrores)
        val tvTiempo = findViewById<TextView>(R.id.labelTiempo)

        btnRePlay = findViewById(R.id.btnTryAgain)
        btnExit = findViewById(R.id.btnExit)

        tvJugador.text = jugador.nombre
        tvAciertos.text = partida.aciertos.toString()
        tvErrores.text = partida.errores.toString()

        val mins = partida.gameTime ?: 0
        val diffMillis = partida.fechaHoraFin!!.time - partida.fechaHoraInicio.time
        val secs = ((diffMillis / 1000) % 60).toInt()
        partida.gameTime = (diffMillis / 1000).toInt()

        tvTiempo.text = "${mins}m ${secs}s"
        val partidaCompletada = UserGameData(
            partida.rondas,
            partida.dificultad,
            partida.aciertos,
            partida.errores,
            partida.fechaHoraInicio,
            partida.fechaHoraFin,
            partida.gameTime
        )
        jugadores = FilesManager.readFile(this)
        agregarPartida(jugador.nombre, jugador.edad, partidaCompletada)
        FilesManager.saveFile(this, jugadores)

        btnRePlay.setOnClickListener {
            onPause()
            startActivity(Intent(this, RegisterActivity::class.java))

        }
        btnExit.setOnClickListener {
            onPause()
            finishAffinity()
        }

    }
    override fun onPause() {
        super.onPause()
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }
}
