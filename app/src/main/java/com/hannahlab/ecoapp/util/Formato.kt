package com.hannahlab.ecoapp.util

import java.text.NumberFormat

object Formato {
    private val moneda = NumberFormat.getCurrencyInstance(Fechas.LOCALE)

    fun soles(monto: Double): String = moneda.format(monto)

    fun horas(horas: Double): String = String.format(Fechas.LOCALE, "%.1f h", horas)
}

/** Planilla por horas: bruto = horas × tarifa; neto = bruto − descuento previsional. */
data class CalculoPlanilla(val horas: Double, val tarifa: Double) {
    val bruto: Double get() = horas * tarifa
    val descuento: Double get() = bruto * DESCUENTO
    val neto: Double get() = bruto - descuento

    companion object {
        /** Porcentaje referencial (ONP) para el cálculo de demostración. */
        const val DESCUENTO = 0.13
    }
}
