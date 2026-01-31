package com.example.collegeschedule.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getWeekDateRange(): Pair<String, String> {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_DATE

    // Если сегодня воскресенье - стартуем с понедельника
    val start = if (today.dayOfWeek.value == 7) today.plusDays(1) else today

    var daysAdded = 0
    var end = start

    // Нужно получить ровно 6 учебных дней (ПН-СБ)
    while (daysAdded < 5) {
        end = end.plusDays(1)
        if (end.dayOfWeek.value != 7) { // Не воскресенье
            daysAdded++
        }
    }

    return start.format(formatter) to end.format(formatter)
}