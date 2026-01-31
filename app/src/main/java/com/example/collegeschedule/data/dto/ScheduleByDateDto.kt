package com.example.collegeschedule.data.dto

data class ScheduleByDateDto(
    val lessonDate: String,
    val weekday: String,
    val lessons: List<LessonDto>
)