package com.example.myapplication

import UserGameData
import java.io.Serializable

var jugadores = mutableListOf<Jugador>()

class Jugador(
    val nombre: String,
    val edad: Int,
    var partidas: MutableList<UserGameData> = mutableListOf()
) : Serializable

fun agregarPartida(nombre: String, edad: Int, nuevaPartida: UserGameData) {
    val jugadorExistente = jugadores.find { it.nombre == nombre }

    if (jugadorExistente != null) {
        jugadorExistente.partidas.add(nuevaPartida)
    } else {
        val nuevoJugador = Jugador(
            nombre = nombre,
            edad = edad,
            partidas = mutableListOf(nuevaPartida)
        )
        jugadores.add(nuevoJugador)
    }
}