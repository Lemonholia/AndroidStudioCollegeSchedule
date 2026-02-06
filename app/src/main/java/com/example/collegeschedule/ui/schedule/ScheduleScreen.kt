package com.example.collegeschedule.ui.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.collegeschedule.data.dto.ScheduleByDateDto
import com.example.collegeschedule.data.dto.LessonDto
import com.example.collegeschedule.data.dto.LessonPartDto
import com.example.collegeschedule.data.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    repository: ScheduleRepository,
    isFavorite: (String) -> Boolean,
    toggleFavorite: (String) -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    var groups by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var schedule by remember { mutableStateOf<List<ScheduleByDateDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var scheduleLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Загружаем список групп
    LaunchedEffect(Unit) {
        try {
            groups = repository.loadGroups()
        } catch (e: Exception) {
            error = "Ошибка загрузки групп"
            snackbarHostState.showSnackbar("Не удалось загрузить список групп")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Расписание") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Кнопка перехода в избранное
                    IconButton(
                        onClick = onNavigateToFavorites
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Избранное",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedGroup != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!scheduleLoading) {
                            scheduleLoading = true
                            error = null
                            scope.launch {
                                try {
                                    schedule = repository.loadSchedule(selectedGroup!!, "", "")
                                } catch (e: Exception) {
                                    try {
                                        val today = LocalDate.now()
                                        val startDate = today.toString()
                                        val endDate = today.plusDays(6).toString()
                                        schedule = repository.loadSchedule(selectedGroup!!, startDate, endDate)
                                    } catch (e2: Exception) {
                                        error = "Ошибка загрузки расписания"
                                        snackbarHostState.showSnackbar("Ошибка загрузки расписания")
                                    }
                                } finally {
                                    scheduleLoading = false
                                }
                            }
                        }
                    },
                    expanded = !scheduleLoading,
                    icon = {
                        if (scheduleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = "Показать расписание"
                            )
                        }
                    },
                    text = { Text("Показать расписание") },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Карточка выбора группы
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Выберите группу",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Кнопка добавления в избранное (если группа выбрана)
                        if (selectedGroup != null) {
                            IconButton(
                                onClick = {
                                    toggleFavorite(selectedGroup!!)
                                    scope.launch {
                                        if (isFavorite(selectedGroup!!)) {
                                            snackbarHostState.showSnackbar("Группа добавлена в избранное")
                                        } else {
                                            snackbarHostState.showSnackbar("Группа удалена из избранного")
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite(selectedGroup!!)) {
                                        Icons.Filled.Favorite
                                    } else {
                                        Icons.Outlined.FavoriteBorder
                                    },
                                    contentDescription = if (isFavorite(selectedGroup!!)) {
                                        "Удалить из избранного"
                                    } else {
                                        "Добавить в избранное"
                                    },
                                    tint = if (isFavorite(selectedGroup!!)) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Поле выбора группы
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedGroup ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            placeholder = { Text("Выберите группу") },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Группа") }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(group)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            if (isFavorite(group)) {
                                                Icon(
                                                    Icons.Filled.Favorite,
                                                    contentDescription = "В избранном",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedGroup = group
                                        expanded = false
                                        schedule = emptyList()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Groups,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Состояния загрузки и ошибок
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch {
                                loading = true
                                error = null
                                try {
                                    groups = repository.loadGroups()
                                } catch (e: Exception) {
                                    error = e.message
                                } finally {
                                    loading = false
                                }
                            }
                        }) {
                            Text("Повторить")
                        }
                    }
                }

                schedule.isNotEmpty() -> {
                    // Заголовок расписания
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Расписание",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ) {
                            Text("${schedule.size} дней")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Список расписания
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(schedule) { daySchedule ->
                            ScheduleDayCard(
                                schedule = daySchedule,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                scheduleLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Загрузка расписания...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                else -> {
                    if (selectedGroup != null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Нажмите кнопку ниже для загрузки расписания",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleDayCard(
    schedule: ScheduleByDateDto,
    modifier: Modifier = Modifier
) {
    val date = schedule.lessonDate ?: "Дата не указана"
    val lessons = schedule.lessons ?: emptyList()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedDate = try {
                    if (date != "Дата не указана") {
                        val localDate = LocalDate.parse(date)
                        localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy (EEEE)"))
                    } else {
                        date
                    }
                } catch (e: Exception) {
                    date
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                SmallChip(
                    label = { Text("${lessons.size} пар") },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (lessons.isEmpty()) {
                Text(
                    text = "Нет занятий",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lessons.forEachIndexed { index, lesson ->
                        if (lesson != null) {
                            LessonItem(
                                lesson = lesson,
                                index = index + 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallChip(
    label: @Composable () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            label()
        }
    }
}

@Composable
fun LessonItem(
    lesson: LessonDto,
    index: Int,
    modifier: Modifier = Modifier
) {
    val safeLessonNumber = lesson.lessonNumber ?: index
    val safeTime = lesson.time ?: "Время не указано"
    val safeSubject = getDisplaySubject(lesson)
    val safeTeacher = getDisplayTeacher(lesson)
    val safeTeacherPosition = getDisplayTeacherPosition(lesson)
    val safeClassroom = getDisplayClassroom(lesson)
    val safeBuilding = getDisplayBuilding(lesson)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("$safeLessonNumber")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = safeTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = safeSubject,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (safeTeacher.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = safeTeacher,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (safeTeacherPosition.isNotEmpty()) {
                        Text(
                            text = safeTeacherPosition,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                if (safeClassroom.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = safeClassroom,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (safeBuilding.isNotEmpty()) {
                                Text(
                                    text = "Корпус: $safeBuilding",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getDisplaySubject(lesson: LessonDto): String {
    if (!lesson.subject.isNullOrEmpty()) {
        return lesson.subject
    }

    val subjects = mutableSetOf<String>()

    lesson.groupParts.forEach { (_, part) ->
        if (part != null && !part.subject.isNullOrEmpty()) {
            subjects.add(part.subject)
        }
    }

    return when {
        subjects.isNotEmpty() -> {
            if (subjects.size == 1) {
                subjects.first()
            } else {
                subjects.joinToString(" / ")
            }
        }
        !lesson.teacher.isNullOrEmpty() -> "Занятие с ${lesson.teacher}"
        else -> "Занятие"
    }
}

private fun getDisplayTeacher(lesson: LessonDto): String {
    if (!lesson.teacher.isNullOrEmpty()) {
        return lesson.teacher
    }

    val teachers = mutableSetOf<String>()

    lesson.groupParts.forEach { (_, part) ->
        if (part != null && !part.teacher.isNullOrEmpty()) {
            teachers.add(part.teacher)
        }
    }

    return when {
        teachers.isNotEmpty() -> {
            if (teachers.size == 1) {
                teachers.first()
            } else {
                teachers.joinToString(" / ")
            }
        }
        else -> ""
    }
}

private fun getDisplayTeacherPosition(lesson: LessonDto): String {
    if (!lesson.teacherPosition.isNullOrEmpty()) {
        return lesson.teacherPosition
    }

    val positions = mutableSetOf<String>()

    lesson.groupParts.forEach { (_, part) ->
        if (part != null && !part.teacherPosition.isNullOrEmpty()) {
            positions.add(part.teacherPosition)
        }
    }

    return when {
        positions.isNotEmpty() -> {
            if (positions.size == 1) {
                positions.first()
            } else {
                positions.joinToString(" / ")
            }
        }
        else -> ""
    }
}

private fun getDisplayClassroom(lesson: LessonDto): String {
    if (!lesson.classroom.isNullOrEmpty()) {
        return lesson.classroom
    }

    val classrooms = mutableSetOf<String>()

    lesson.groupParts.forEach { (_, part) ->
        if (part != null && !part.classroom.isNullOrEmpty()) {
            classrooms.add(part.classroom)
        }
    }

    return when {
        classrooms.isNotEmpty() -> {
            if (classrooms.size == 1) {
                classrooms.first()
            } else {
                classrooms.joinToString(" / ")
            }
        }
        else -> ""
    }
}

private fun getDisplayBuilding(lesson: LessonDto): String {
    if (!lesson.building.isNullOrEmpty()) {
        return lesson.building
    }

    val buildings = mutableSetOf<String>()

    lesson.groupParts.forEach { (_, part) ->
        if (part != null && !part.building.isNullOrEmpty()) {
            buildings.add(part.building)
        }
    }

    return when {
        buildings.isNotEmpty() -> {
            if (buildings.size == 1) {
                buildings.first()
            } else {
                buildings.joinToString(" / ")
            }
        }
        else -> ""
    }
}