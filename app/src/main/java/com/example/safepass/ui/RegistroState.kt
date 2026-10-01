package com.example.safepass.ui

import com.example.safepass.data.Asistente

sealed class RegistroState {
    data object Idle : RegistroState()

    data class Success(
        val asistente: Asistente,
        val precioFinal: Double,
        val resumen: String
    ) : RegistroState()

    data class Error(val mensaje: String) : RegistroState()
}