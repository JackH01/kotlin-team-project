package com.tripwizard.ui.tripdetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.window.Dialog
import com.tripwizard.R
import com.tripwizard.data.Attraction
import com.tripwizard.data.Priority
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditAttraction(
    updateAttractionInDb: (Attraction) -> Unit,
    onDismissRequest: () -> Unit,
    currentAttraction: Attraction
) {
    BackHandler {
        onDismissRequest()
    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.dialog_elevation))
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(dimensionResource(R.dimen.padding_medium))

            ) {
                Text(
                    text = "Attraction Details:"
                )

                var updatedName by rememberSaveable { mutableStateOf(currentAttraction.name) }
                OutlinedTextField(
                    label = { Text("Attraction Name") },
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_medium)),
                )

                var updatedDescription by rememberSaveable { mutableStateOf(currentAttraction.description) }
                OutlinedTextField(
                    label = { Text("Attraction Description") },
                    value = updatedDescription,
                    onValueChange = { updatedDescription = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                )
                Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                var datePickerState = rememberDatePickerState()
                var dateTextbox = rememberSaveable { mutableStateOf(currentAttraction.date.format(DateTimeFormatter.ofPattern( "dd/MM/uuuu" ))).value }
                var showDialog = rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    value = if (datePickerState.selectedDateMillis == null) {
                        dateTextbox
                    } else {
                        convertToLocalDate(datePickerState).format(DateTimeFormatter.ofPattern( "dd/MM/uuuu" ))
                    },
                    onValueChange = {  },
                    enabled = false,
                    label = { Text("Due Date") },
                    modifier = Modifier.clickable { showDialog.value = true }
                )

                if (showDialog.value) {
                    DatePickerDialog(
                        onDismissRequest = { showDialog.value = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog.value = false
                            }) {
                                Text("Ok")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                var updatedPriority by remember { mutableStateOf(currentAttraction.priority) }
                EditAttractionPriorityDropDown (
                    selectedPriority = updatedPriority,
                    onPrioritySelected = { updatedPriority = it }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() }
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            val editedAttraction = currentAttraction.copy(
                                name = updatedName,
                                description = updatedDescription,
                                date =
                                if (datePickerState.selectedDateMillis != null) {
                                    convertToLocalDate(datePickerState)
                                } else {
                                    currentAttraction.date
                                },
                                priority = updatedPriority
                            )
                            val test = Attraction(123,"test", "desc", LocalDate.now(),2,true,Priority.MEDIUM)
                            updateAttractionInDb(editedAttraction)
                            onDismissRequest()
                        }
                    ) {
                        Text("Edit Attraction")
                    }
                }
            }
        }
    }
}

@Composable
fun EditAttractionPriorityDropDown(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded =!expanded }) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selectedPriority.text,
            onValueChange = {  },
            label = { Text("Priority") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            for (enumValue in enumValues<Priority>()) {
                DropdownMenuItem(
                    text = { Text(text = enumValue.text) },
                    onClick = {
                        expanded = false
                        onPrioritySelected(enumValue)
                    }
                )
            }

        }
    }
}
fun editAttractionConvertToLocalDate(datePickerState: DatePickerState): LocalDate {
    return Instant.ofEpochMilli(datePickerState.selectedDateMillis!!).atZone(ZoneId.systemDefault()).toLocalDate()
}