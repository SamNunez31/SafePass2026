package com.example.safepass.logic

import android.util.Log
import com.example.safepass.data.Asistente
import com.example.safepass.ui.RegistroState

// ---------- Reglas del evento (un solo lugar para cambiarlas) ----------

const val EDAD_MINIMA = 18   // mayoría de edad: solo ingresan adultos
const val EDAD_MAXIMA = 95   // tope realista contra errores de tipeo

// ---------- Extension Functions ----------

/** Regla de seguridad del evento: solo mayores de edad. */
fun Int.esMayorDeEdad(): Boolean = this >= EDAD_MINIMA

/** Edad humana razonable: filtra errores de tipeo como 0, -5 o 250. */
fun Int.esEdadRealista(): Boolean = this in 1..EDAD_MAXIMA

/** Un nombre es válido si tiene al menos 3 caracteres y solo letras o espacios. */
fun String.esNombreValido(): Boolean =
    this.trim().length >= 3 && this.trim().all { it.isLetter() || it == ' ' }

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
        return RegistroState.Error("Nombre inválido: use solo letras (mínimo 3 caracteres).")
    }

    // 2. Entrada segura: toIntOrNull() evita el crash si escriben letras o dejan vacío.
    //    let solo se ejecuta si el resultado NO es nulo.
    //    Elvis (?:) sale con Error si el valor es nulo o no es una edad realista.
    val edad: Int = edadTexto.trim().toIntOrNull()
        ?.let { if (it.esEdadRealista()) it else null }
        ?: return RegistroState.Error("Edad inválida: ingrese un número entero entre $EDAD_MINIMA y $EDAD_MAXIMA.")

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