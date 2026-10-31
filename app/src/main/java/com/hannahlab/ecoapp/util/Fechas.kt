package com.hannahlab.ecoapp.util

import androidx.annotation.StringRes
import com.hannahlab.ecoapp.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class Periodo { HOY, SEMANA, MES }

/** Rangos de fechas como epochDay, que es como se guardan en Room. */
object Fechas {
    val LOCALE: Locale = Locale.forLanguageTag("es-PE")

    private val formatoCorto = DateTimeFormatter.ofPattern("d MMM", LOCALE)

    fun hoy(): LocalDate = LocalDate.now()

    fun hoyEpoch(): Long = hoy().toEpochDay()

    fun inicioMes(): Long = hoy().withDayOfMonth(1).toEpochDay()

    fun diasDelMes(): Int = hoy().lengthOfMonth()

    fun rango(periodo: Periodo): LongRange {
        val hoy = hoy()
        val desde = when (periodo) {
            Periodo.HOY -> hoy
            Periodo.SEMANA -> hoy.with(DayOfWeek.MONDAY)
            Periodo.MES -> hoy.withDayOfMonth(1)
        }
        return desde.toEpochDay()..hoy.toEpochDay()
    }

    fun semanaPasada(): LongRange {
        val lunes = hoy().with(DayOfWeek.MONDAY).minusWeeks(1)
        return lunes.toEpochDay()..lunes.plusDays(6).toEpochDay()
    }

    /** Semana del mes (0..4) a la que pertenece un día. */
    fun semanaDelMes(epochDay: Long): Int = (LocalDate.ofEpochDay(epochDay).dayOfMonth - 1) / 7

    fun nombreMes(): String {
        val hoy = hoy()
        val mes = hoy.month.getDisplayName(TextStyle.FULL_STANDALONE, LOCALE)
            .replaceFirstChar { it.titlecase(LOCALE) }
        return "$mes ${hoy.year}"
    }

    fun formatoCorto(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(formatoCorto)

    @StringRes
    fun saludo(): Int {
        val hora = LocalTime.now().hour
        return when {
            hora < 12 -> R.string.saludo_manana
            hora < 19 -> R.string.saludo_tarde
            else -> R.string.saludo_noche
        }
    }
}
