package com.example.collegeschedule.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelector(
    groups: List<String>,
    selectedGroup: String?,
    onGroupSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = true
            },
            label = { Text("Выберите группу") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val filtered = if (searchQuery.isEmpty()) {
                groups
            } else {
                groups.filter { it.contains(searchQuery, ignoreCase = true) }
            }

            filtered.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group) },
                    onClick = {
                        onGroupSelected(group)
                        searchQuery = group
                        expanded = false
                    }
                )
            }
        }
    }
}
