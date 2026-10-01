package com.example.safepass.logic

import android.util.Log
import com.example.safepass.data.Asistente
import com.example.safepass.ui.RegistroState

// ---------- Extension Functions ----------

fun Int.esMayorDeEdad(): Boolean = this >= 18

fun String.esNombreValido(): Boolean = this.isNotBlank() && this.trim().length >= 2

fun String.precioBase(): Double = when (this) {
    "VIP" -> 120.0
    "Estudiante" -> 30.0
    else -> 50.0
}

// ---------- Higher-Order Function ----------

fun procesarRegistro(
    nombre: String,
    edadTexto: String,
    tipoEntrada: String,
    descuento: (Asistente) -> Int
): RegistroState {

    if (!nombre.esNombreValido()) {
        return RegistroState.Error("El nombre no puede estar vacío (mínimo 2 caracteres).")
    }

    val edad: Int = edadTexto.trim().toIntOrNull()
        ?.let { if (it in 0..120) it else null }
        ?: return RegistroState.Error("La edad debe ser un número entero válido (0-120).")

    if (!edad.esMayorDeEdad()) {
        return RegistroState.Error("Acceso denegado: el asistente es menor de edad ($edad años).")
    }

    val asistente = Asistente(
        nombre = nombre.trim(),
        edad = edad,
        tipoEntrada = tipoEntrada
    ).apply {
        Log.d("SafePass", "Asistente creado: $this")
    }

    val precioFinal = asistente.run {
        val porcentaje = descuento(this).coerceIn(0, 100)
        tipoEntrada.precioBase() * (100 - porcentaje) / 100.0
    }

    val resumen = StringBuilder().apply {
        appendLine("Nombre: ${asistente.nombre}")
        appendLine("Edad: ${asistente.edad ?: "N/D"}")
        appendLine("Entrada: ${asistente.tipoEntrada}")
        append("Total a pagar: $${String.format("%.2f", precioFinal)}")
    }.toString()

    return RegistroState.Success(asistente, precioFinal, resumen)
}