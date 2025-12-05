package com.example.myapplication

import UserGameData
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Date

class GameActivity_2 : AppCompatActivity() {
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
    private lateinit var allQuestions: List<PreguntaJuego>
    private var currentQuestionIndex = 0
    private var score = 0
    private var isAnswered = false

    private lateinit var jugador: Jugador
    private lateinit var partida: UserGameData
    private lateinit var partidaActual: UserGameData

    private var countDownTimer: android.os.CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity2)

        mediaPlayer = MediaPlayer.create(this, R.raw.playfull)
        mediaPlayer.start()

        // Inicializar views
        labelCuentaAtras = findViewById(R.id.labelCuentaAtras)
        labelCuentaAtras.visibility = View.INVISIBLE
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


        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        jugador = (intent.getSerializableExtra("JUGADOR") as? Jugador)!!



        partidaActual = jugador.partidas.lastOrNull()!!



        partida = partidaActual

        // Justo antes de asignar allQuestions
        Log.d("GameActivity_2", "Dificultad partida recibida: ${partida.dificultad}")

        allQuestions = try {
            when (partida.dificultad) {
                1 -> {
                    val questions = PreguntaJuego.loadQuestionsFromJson(this, "nivel1.json")
                    Log.d("GameActivity_2", "Preguntas nivel1 cargadas: ${questions.size}")
                    questions
                }
                2 -> {
                    val questions = PreguntaJuego.loadQuestionsFromJson(this, "nivel2.json")
                    Log.d("GameActivity_2", "Preguntas nivel2 cargadas: ${questions.size}")
                    questions
                }
                else -> {
                    Log.e("GameActivity_2", "Dificultad inválida: ${partida.dificultad}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("GameActivity_2", "Error cargando preguntas: ${e.message}")
            emptyList()
        }

// Validar que no esté vacía antes de continuar
        if (allQuestions.isEmpty()) {
            Log.e("GameActivity_2", "No se pudieron cargar preguntas. Se finalizará la actividad.")
            finish() // Evita que la app se caiga
            return
        }


        allQuestions = when (this.partida.dificultad) {
            1 -> PreguntaJuego.loadQuestionsFromJson(this, "nivel1.json")
            2 -> PreguntaJuego.loadQuestionsFromJson(this, "nivel2.json")
            else -> {
                Log.e("GameActivity_2", "Dificultad inválida: ${partida.dificultad}")
                emptyList()
            }
        }
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
            onPause()
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
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun onAnswerSelected(selectedButton: Button) {
        val question = gameQuestions[currentQuestionIndex]
        isAnswered = true

        // Llama a checkAnswer pasando también el contexto
        val correct = gameMechanics.checkAnswer(
            selectedButton,
            allButtons,
            allContainers,
            question, this)

        if (correct) score++

        currentQuestionIndex++

        btnNextRound.visibility = View.VISIBLE
        btnNextRound.text =
            if (currentQuestionIndex >= gameQuestions.size) "VER RESULTADOS"
            else "SIGUIENTE RONDA"
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

    override fun onPause() {
        super.onPause()
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }
}
