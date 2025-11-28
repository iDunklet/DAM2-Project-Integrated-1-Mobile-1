package com.example.myapplication

import UserGameData
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Date
import android.os.Handler
import android.os.Looper

class GameActivity_ds : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var labelCuentaAtras: TextView
    private lateinit var labelTextoPregunta: TextView
    private lateinit var allContainers: List<FrameLayout>
    private lateinit var labelNumRonda: TextView
    private lateinit var labelNumTotalRondas: TextView
    private lateinit var btnNextRound: Button
    private lateinit var allButtons: List<Button>

    private lateinit var gameMechanics: GameMechanics
    private lateinit var gameQuestions: List<PreguntaJuego>
    private var currentQuestionIndex = 0
    private var score = 0
    private var isAnswered = false

    private lateinit var jugador: Jugador
    private lateinit var partida: UserGameData
    private lateinit var partidaActual: UserGameData
    private var countDownTimer: android.os.CountDownTimer? = null
    private val ROUND_TIME_SECONDS: Long = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activityds)
        mediaPlayer = MediaPlayer.create(this, R.raw.colorfull)
        mediaPlayer.start()

        // Inicializar views
        labelCuentaAtras = findViewById(R.id.labelCuentaAtras)
        labelTextoPregunta = findViewById(R.id.labelTextoPregunta1)
        labelNumRonda = findViewById(R.id.labelNumRonda)
        labelNumTotalRondas = findViewById(R.id.labelNumTotalRondas)
        btnNextRound = findViewById(R.id.buttonRegisterAceptar)

        val containerBtn1: FrameLayout = findViewById(R.id.containerBtn1)
        val containerBtn2: FrameLayout = findViewById(R.id.containerBtn2)
        val containerBtn3: FrameLayout = findViewById(R.id.containerBtn3)
        val containerBtn4: FrameLayout = findViewById(R.id.containerBtn4)
        val containerBtn5: FrameLayout = findViewById(R.id.containerBtn5)
        allContainers = listOf(containerBtn1, containerBtn2, containerBtn3, containerBtn4, containerBtn5)

        val btnBox1: Button = findViewById(R.id.btnBox1)
        val btnBox2: Button = findViewById(R.id.btnBox2)
        val btnBox3: Button = findViewById(R.id.btnBox3)
        val btnBox4: Button = findViewById(R.id.btnBox4)
        val btnBox5: Button = findViewById(R.id.btnBox5)
        allButtons = listOf(btnBox1, btnBox2, btnBox3, btnBox4, btnBox5)

        val btnBack: ImageButton = findViewById(R.id.IconBack)
        btnBack.setOnClickListener { finish() }



        gameMechanics = GameMechanics(this)

        val allQuestions = PreguntaJuego.loadQuestionsFromJson(this, "nivel3.json")

        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        jugador = (intent.getSerializableExtra("JUGADOR") as? Jugador)!!

        partidaActual = jugador.partidas.lastOrNull()!!


        partida = partidaActual
        val rawRondas = partida.rondas
        val totalRondas = rawRondas.coerceIn(1, allQuestions.size)
        gameQuestions = allQuestions.shuffled().take(totalRondas)
        labelNumTotalRondas.text = totalRondas.toString()

        allButtons.forEach { button ->
            button.setOnClickListener {
                if (!isAnswered) onAnswerSelected(it as Button)
            }
        }

        btnNextRound.setOnClickListener {
            if (currentQuestionIndex >= gameQuestions.size) endGame()
            else loadQuestion()
        }

        loadQuestion()
    }

    private fun loadQuestion() {
        if (currentQuestionIndex >= gameQuestions.size) {
            endGame()
            return
        }

        val question = gameQuestions[currentQuestionIndex]
        labelNumRonda.text = (currentQuestionIndex + 1).toString()
        labelTextoPregunta.text = question.enunciadoEs

        // 1. Resetear contenedores
        allContainers.forEach { container ->
            container.background = ContextCompat.getDrawable(this, R.drawable.edit_text_radius)
        }

        // 2. Resetear botones (tamaño + habilitación)
        allButtons.forEach { button ->
            button.isEnabled = true

            val params = button.layoutParams
            params.width = FrameLayout.LayoutParams.MATCH_PARENT
            params.height = dpToPx(250)
            button.layoutParams = params
            button.requestLayout()
        }


        // 3. Configurar la pregunta (ESTO ya pone colores, imágenes o textos)
        gameMechanics.setupQuestion(allButtons, allContainers, question)

        // 4. Estado del juego
        isAnswered = false
        btnNextRound.visibility = View.INVISIBLE
        startSimpleTimer()
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun onAnswerSelected(selectedButton: Button) {
        val question = gameQuestions[currentQuestionIndex]
        isAnswered = true

        // Llama a checkAnswer (esto marca visualmente la respuesta correcta/incorrecta)
        val correct = gameMechanics.checkAnswer(
            selectedButton,
            allButtons,
            allContainers,
            question, this)

        if (correct) {
            score++
        } else {
            endGame()
        }

        allButtons.forEach { it.isEnabled = false }


        btnNextRound.visibility = View.VISIBLE


        btnNextRound.text = if (correct) "¡CORRECTO! (Avanzando...)" else "FALLO. (Avanzando...)"

        Handler(Looper.getMainLooper()).postDelayed({

            currentQuestionIndex++

            if (currentQuestionIndex >= gameQuestions.size) {
                endGame()
            } else {
                loadQuestion()
            }

        }, 3000)

    }

    private fun endGame() {

        partida.aciertos = score
        partida.errores = (partida.rondas - score)
        partida.fechaHoraFin = Date()

        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("JUGADOR", jugador)
        startActivity(intent)

        btnNextRound.text = "FINALIZAR"
        btnNextRound.setOnClickListener { finish() }
    }

    private fun startSimpleTimer() {
        countDownTimer?.cancel()

        val totalTime = ROUND_TIME_SECONDS * 1000

        countDownTimer = object : android.os.CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                labelCuentaAtras.text = seconds.toString()

                labelCuentaAtras.setTextColor(
                    if (seconds <= 4)
                        ContextCompat.getColor(this@GameActivity_ds, R.color.state_error)
                    else
                        ContextCompat.getColor(this@GameActivity_ds, R.color.faded_gold)
                )
            }

            override fun onFinish() {
                labelCuentaAtras.text = "0"

                if (!isAnswered) {
                    allButtons.forEach { it.isEnabled = false }
                    onAnswerSelected(allButtons[0])
                    endGame()
                }
            }
        }.start()

    }
    override fun onPause() {
        super.onPause()
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }
}
