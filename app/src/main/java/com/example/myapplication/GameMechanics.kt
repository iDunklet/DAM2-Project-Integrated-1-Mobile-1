package com.example.myapplication

import android.content.Context
import android.widget.Button
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import android.widget.FrameLayout

class GameMechanics(private val context: Context) {
    val randomColors = listOf(
        R.color.naranja,
        R.color.azulOscuro,
        R.color.amarillo,
        R.color.marron,
        R.color.purple
                                     )

    /*
    val randomColors = listOf(
        R.color.pastel_blue,
        R.color.pastel_green,
        R.color.pastel_yellow,
        R.color.pastel_orange,
        R.color.pastel_pink
    )
    */


    /**
     * Configura los botones según la pregunta y la categoría.
     * Aplica colores aleatorios para NUMBER y SHAPE, y para COLOR deja fondo aleatorio.
     * Guarda el valor real de cada botón en tag.
     */
    fun setupQuestion(
        buttons: List<Button>,
        containers: List<FrameLayout>,
        question: PreguntaJuego
    ) {
        val shuffledOptions = question.opcionesEn?.shuffled().orEmpty()
        val shuffledColors = randomColors.shuffled()

        buttons.zip(shuffledOptions).forEach { (button, option) ->
            button.tag = option
            button.isEnabled = true
            button.foreground = null
        }

        when (question.categoria.uppercase()) {

            "NUMBER" -> {
                buttons.zip(shuffledOptions).forEachIndexed { index, (button, option) ->
                    button.text = mapNumber(option)
                    button.textSize = 100f
                    button.setTextColor(ContextCompat.getColor(context, R.color.negro))

                    val color = shuffledColors[index % shuffledColors.size]
                    button.setBackgroundColor(ContextCompat.getColor(context, color))
                }
            }

            "SHAPE" -> {
                buttons.zip(shuffledOptions).forEachIndexed { index, (button, option) ->
                    button.text = ""
                    button.setBackgroundResource(mapShape(option))

                    val color = shuffledColors[index % shuffledColors.size]
                    containers[index].setBackgroundColor(
                        ContextCompat.getColor(context, color)
                    )
                }
            }

            "COLOR" -> {
                buttons.zip(shuffledOptions).forEach { (button, option) ->
                    button.text = ""
                    button.setBackgroundColor(
                        ContextCompat.getColor(context, mapColor(option))
                    )
                }
            }

            else -> {
                buttons.zip(shuffledOptions).forEachIndexed { index, (button, option) ->
                    button.text = option
                    val color = shuffledColors[index % shuffledColors.size]
                    button.setBackgroundColor(ContextCompat.getColor(context, color))
                }
            }
        }
    }

    /**
     * Comprueba la respuesta seleccionada y aplica los foregrounds correspondientes.
     * Devuelve true si la respuesta era correcta, false si no.
     */
    fun checkAnswer(
        selectedButton: Button,
        buttons: List<Button>,
        containers: List<FrameLayout>,
        question: PreguntaJuego,
        context: Context
                   ): Boolean {
        val selectedTag = selectedButton.tag?.toString() ?: ""
        val correctAnswer = question.respuestaCorrectaEn

        val isCorrect = isAnswerCorrect(selectedTag, correctAnswer)

        buttons.forEach { it.isEnabled = false }

        val selectedIndex = buttons.indexOf(selectedButton)
        
        val correctIndex = buttons.indexOfFirst {
            isAnswerCorrect(it.tag?.toString() ?: "", correctAnswer)
        }

        if (isCorrect) {
            when(question.categoria.uppercase()) {
                "NUMBER" -> {
                    selectedButton.background = getDrawable("boton_verde")
                }
                "SHAPE" -> {
                    containers[selectedIndex].background = getDrawable("boton_verde")
                }
                "COLOR" -> {
                    containers[selectedIndex].background = getDrawable("boton_verde")
                    selectedButton.layoutParams.width = 200
                    selectedButton.layoutParams.height = 200
                }
            }

            buttons.forEachIndexed { index, button ->
                if (index != selectedIndex && index != correctIndex) {
                    button.background = getDrawable("boton_base")
                }

            }

        } else {

            when(question.categoria.uppercase()) {
                "NUMBER" -> {
                    selectedButton.background = getDrawable("boton_rojo")
                }
                "SHAPE" -> {
                    containers[selectedIndex].background = getDrawable("boton_rojo")
                }
                "COLOR" -> {
                    containers[selectedIndex].background = getDrawable("boton_rojo")
                    selectedButton.layoutParams.width = 200
                    selectedButton.layoutParams.height = 200
                }
            }

            val correctIndex = buttons.indexOfFirst {
                isAnswerCorrect(it.tag?.toString() ?: "", correctAnswer)
            }
            if (correctIndex != -1) {
                when(question.categoria.uppercase()) {
                    "NUMBER" -> {
                        buttons[correctIndex].background = getDrawable("boton_verde")
                    }

                    "SHAPE" -> {
                        containers[correctIndex].background = getDrawable("boton_verde")
                    }

                    "COLOR" -> {
                        containers[correctIndex].background = getDrawable("boton_verde")
                        buttons[correctIndex].layoutParams.width = 200
                        buttons[correctIndex].layoutParams.height = 200
                    }
                }

            }

            buttons.forEachIndexed { index, button ->
                if (index != selectedIndex && index != correctIndex) {
                    button.background = getDrawable("boton_base")
                }


            }

        }

        return isCorrect
    }


    /** Normaliza números escritos como texto para la categoría NUMBER */
    fun isAnswerCorrect(selected: String, correct: String): Boolean {
        val map = mapOf(
            "0" to "ZERO", "1" to "ONE", "2" to "TWO", "3" to "THREE", "4" to "FOUR",
            "5" to "FIVE", "6" to "SIX", "7" to "SEVEN", "8" to "EIGHT", "9" to "NINE",
            "10" to "TEN", "12" to "TWELVE", "16" to "SIXTEEN", "25" to "TWENTY-FIVE",
            "30" to "THIRTY", "32" to "THIRTY-TWO", "56" to "FIFTY-SIX"
                       )
        val normalizedSelected = map[selected.uppercase()] ?: selected.uppercase()
        return normalizedSelected == correct.uppercase()
    }

    /** Map número escrito a número visual */
    private fun mapNumber(option: String): String {
        return when (option.uppercase()) {
            "ZERO" -> "0"
            "ONE" -> "1"
            "TWO" -> "2"
            "THREE" -> "3"
            "FOUR" -> "4"
            "FIVE" -> "5"
            "SIX" -> "6"
            "SEVEN" -> "7"
            "EIGHT" -> "8"
            "NINE" -> "9"
            "TEN" -> "10"
            "TWELVE" -> "12"
            "SIXTEEN" -> "16"
            "TWENTY-FIVE" -> "25"
            "THIRTY" -> "30"
            "THIRTY-TWO" -> "32"
            "FIFTY-SIX" -> "56"
            else -> option
        }
    }

    /** Map string a drawable de forma */
    private fun mapShape(option: String): Int {
        return when (option.uppercase()) {
            "CIRCLE" -> R.drawable.shape_circle
            "SQUARE" -> R.drawable.shape_square
            "TRIANGLE" -> R.drawable.shape_triangle
            "RECTANGLE" -> R.drawable.shape_rectangle
            "OVAL" -> R.drawable.shape_oval
            "STAR" -> R.drawable.shape_star
            "HEXAGON" -> R.drawable.shape_hexagon
            "OCTAGON" -> R.drawable.shape_octagon
            "DIAMOND" -> R.drawable.shape_diamond
            "PENTAGON" -> R.drawable.shape_pentagon
            "HEPTAGON" -> R.drawable.shape_heptagon
            "NONAGON" -> R.drawable.shape_nonagon
            "CUBE" -> R.drawable.shape_cube
            "CYLINDER" -> R.drawable.shape_cylinder
            "CONE" -> R.drawable.shape_cone
            "SPHERE" -> R.drawable.shape_sphere
            "PYRAMID" -> R.drawable.shape_pyramid
            "REGULAR_POLYGON" -> R.drawable.shape_regular_polygon
            "IRREGULAR_POLYGON" -> R.drawable.shape_irregular_polygon
            else -> R.drawable.shape_default
        }
    }

    /** Map string a color de Android */
    private fun mapColor(option: String): Int {
        return when (option.uppercase()) {
            "RED" -> R.color.red
            "BLUE" -> R.color.blue
            "GREEN" -> R.color.green
            "YELLOW" -> R.color.yellow
            "BLACK" -> R.color.negro
            "WHITE" -> R.color.blanco
            "PURPLE" -> R.color.purple
            "ORANGE" -> R.color.orange
            "BROWN" -> R.color.brown
            "GREY" -> R.color.gray
            "CYAN" -> R.color.cyan
            "MAGENTA" -> R.color.magenta
            else -> R.color.gray
        }
    }

    /** Obtiene drawable por nombre */
    private fun getDrawable(name: String): Drawable? {
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return ContextCompat.getDrawable(context, resId)
    }


}

