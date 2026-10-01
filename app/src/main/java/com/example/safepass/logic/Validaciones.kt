package com.example.safepass.logic

import android.util.Log
import com.example.safepass.data.Asistente
import com.example.safepass.ui.RegistroState

// ---------- Extension Functions ----------

/** Regla de seguridad del evento: solo mayores de edad. */
fun Int.esMayorDeEdad(): Boolean = this >= 18

/** Edad realista para un asistente: evita errores de tipeo como 0 o 250. */
fun Int.esEdadRealista(): Boolean = this in 1..100

/** Un nombre es válido si tiene al menos 2 caracteres y solo letras o espacios. */
fun String.esNombreValido(): Boolean =
    this.trim().length >= 2 && this.trim().all { it.isLetter() || it == ' ' }

/** Precio base según el tipo de entrada. */
fun String.precioBase(): Double = when (this) {
    "VIP" -> 120.0
    "Estudiante" -> 30.0
    else -> 50.0 // General
}

// ---------- Higher-Order Function ----------

/**
 * Procesa los datos crudos de los campos de texto y devuelve un RegistroState.
 *
 * @param descuento lambda que recibe el Asistente y devuelve el % de descuento de reserva.
 */
fun procesarRegistro(
    nombre: String,
    edadTexto: String,
    tipoEntrada: String,
    descuento: (Asistente) -> Int
): RegistroState {

    // 1. Validación del nombre (extension function)
    if (!nombre.esNombreValido()) {
        return RegistroState.Error("Nombre inválido: use solo letras (mínimo 2 caracteres).")
    }

    // 2. Entrada segura: toIntOrNull() evita el crash si escriben letras o dejan vacío.
    //    let solo se ejecuta si el resultado NO es nulo.
    //    Elvis (?:) sale con Error si el valor es nulo o no es una edad realista.
    val edad: Int = edadTexto.trim().toIntOrNull()
        ?.let { if (it.esEdadRealista()) it else null }
        ?: return RegistroState.Error("Ingrese una edad válida (número entero entre 1 y 100).")

    // 3. Regla de negocio: mayoría de edad (extension function sobre Int)
    if (!edad.esMayorDeEdad()) {
        return RegistroState.Error("Acceso denegado: el asistente es menor de edad ($edad años).")
    }

    // 3b. (Opcional) Coherencia entre edad y tipo de entrada
    if (tipoEntrada == "Estudiante" && edad > 35) {
        return RegistroState.Error("La entrada Estudiante aplica hasta los 35 años.")
    }

    // 4. apply: configura/inspecciona el objeto recién creado y lo devuelve.
    val asistente = Asistente(
        nombre = nombre.trim(),
        edad = edad,
        tipoEntrada = tipoEntrada
    ).apply {
        Log.d("SafePass", "Asistente creado: $this")
    }

    // 5. run: calcula el precio final usando el asistente como contexto (this).
    val precioFinal = asistente.run {
        val porcentaje = descuento(this).coerceIn(0, 100)
        tipoEntrada.precioBase() * (100 - porcentaje) / 100.0
    }

    // 6. apply sobre StringBuilder para armar el resumen con plantillas de cadena.
    val resumen = StringBuilder().apply {
        appendLine("Nombre: ${asistente.nombre}")
        appendLine("Edad: ${asistente.edad ?: "N/D"}")
        appendLine("Entrada: ${asistente.tipoEntrada}")
        append("Total a pagar: $${String.format("%.2f", precioFinal)}")
    }.toString()

    return RegistroState.Success(asistente, precioFinal, resumen)
}