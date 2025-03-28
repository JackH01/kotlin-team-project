package com.tripwizard.ui.home

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.tripwizard.R
import com.tripwizard.data.LabelOptions
import com.tripwizard.data.Trip
import com.tripwizard.ui.utils.getLocation
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun EditTrip(
    initialName: String,
    initialDescription: String,
    initialStart: LocalDate,
    initialEnd: LocalDate,
    initialLatitude: Double,
    initialLongitude: Double,
    initialRadius: Float,
    initialDaysBeforeToRemind: Int,
    initialImageUri: Uri?,
    initialLabelOptions: List<LabelOptions>,
    updateTripInDb: (name: String, description: String, start: LocalDate, end: LocalDate, latitude: Double, longitude: Double, radius: Float, daysBeforeToRemind: Int, imageUri: String, labelOptions: List<LabelOptions>) -> Unit,
    onDismissRequest: () -> Unit
) {
    // TODO move camera stuff into view model?
    /* Functionality required for use of camera. */
    // Providing a context to be used by the camera.
    val context: Context = LocalContext.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    if (checkCameraPermission(context)) {
        hasCameraPermission = true
    } else {
        val requestPermissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    hasCameraPermission = true
                } else {
                    Toast.makeText(
                        context,
                        "Camera Permission Denied", Toast.LENGTH_LONG
                    ).show()
                    hasCameraPermission = false
                }
            }
        SideEffect {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var imageUri by remember { mutableStateOf(initialImageUri) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val imageFromGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // Used to save image to database.
        imageUri = uri

        if (uri == null) {
            imageBitmap = null
        } else {
            val contentResolver: ContentResolver = context.contentResolver
            imageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                .asImageBitmap()

            // Saving a copy so we can access when we lose permission to gallery.
            val imgAsBitmap = imageBitmap!!.asAndroidBitmap()
            val file = createImageFile(context)
            file.outputStream().use {
                imgAsBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            imageUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )
        }
    }
    val imageFromCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captured ->
        if (!captured) {
            imageBitmap = null
            imageUri = null
        } else {
            val contentResolver: ContentResolver = context.contentResolver
            imageBitmap = imageUri?.let {
                ImageDecoder.createSource(contentResolver, it)
            }?.let { ImageDecoder.decodeBitmap(it).asImageBitmap() }
        }
    }

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
                    .verticalScroll(
                        rememberScrollState()
                    )

            ) {
                var name by rememberSaveable {
                    mutableStateOf(
                        initialName
                    )
                }
                var description by rememberSaveable {
                    mutableStateOf(
                        initialDescription
                    )
                }

                Text(
                    text = "Trip Details:"
                )
                OutlinedTextField(
                    label = { Text("Trip Name") },
                    value = name,
                    onValueChange = { name = it },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_medium))
                )
                OutlinedTextField(
                    label = { Text("Trip Description") },
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                val startDatePickerState = rememberDatePickerState(
                    initialStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                var openStartDateDialog by rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    value = if (startDatePickerState.selectedDateMillis == null) {
                        "Select Date"
                    } else {
                        convertToLocalDate(startDatePickerState).format(
                            DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy"
                            )
                        )
                    },
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Start Date") },
                    modifier = Modifier.clickable { openStartDateDialog = true }
                )
                if (openStartDateDialog) {
                    DatePickerDialog(
                        onDismissRequest = {
                            openStartDateDialog = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    openStartDateDialog = false
                                }
                            ) {
                                Text("Close")
                            }
                        }
                    ) {
                        DatePicker(state = startDatePickerState)
                    }
                }
                val endDatePickerState = rememberDatePickerState(
                    initialEnd.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                var openEndDateDialog by rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    value = if (endDatePickerState.selectedDateMillis == null) {
                        "Select Date"
                    } else {
                        convertToLocalDate(endDatePickerState).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    },
                    onValueChange = {},
                    enabled = false,
                    label = { Text("End Date") },
                    modifier = Modifier.clickable { openEndDateDialog = true }
                )
                if (openEndDateDialog) {
                    DatePickerDialog(
                        onDismissRequest = {
                            openEndDateDialog = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    openEndDateDialog = false
                                }
                            ) {
                                Text("Close")
                            }
                        }
                    ) {
                        DatePicker(state = endDatePickerState)
                    }
                }

                var daysBeforeToRemind by rememberSaveable {
                    mutableStateOf(initialDaysBeforeToRemind)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = daysBeforeToRemind.toString(),
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Days before to remind") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { daysBeforeToRemind++ }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                    IconButton(onClick = { if (daysBeforeToRemind > 1) daysBeforeToRemind-- }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Add")
                    }
                }

                var permissionCoarseLocation by rememberSaveable {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
                var permissionFineLocation by rememberSaveable {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }

                val requestPermissions =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
                        if (it[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                            permissionCoarseLocation = PackageManager.PERMISSION_GRANTED
                        }
                        if (it[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                            permissionFineLocation = PackageManager.PERMISSION_GRANTED
                        }
                    }

                SideEffect {
                    if (permissionCoarseLocation == PackageManager.PERMISSION_DENIED && permissionFineLocation == PackageManager.PERMISSION_DENIED) {
                        requestPermissions.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                }
                var showLocationSelectorDialog by rememberSaveable {
                    mutableStateOf(false)
                }
                val locationPermissionsEnabled =
                    permissionCoarseLocation == PackageManager.PERMISSION_GRANTED || permissionFineLocation ==
                            PackageManager.PERMISSION_GRANTED
//                var locationSet by rememberSaveable { mutableStateOf(false) }
                var latitude by rememberSaveable { mutableStateOf(initialLatitude) }
                var longitude by rememberSaveable { mutableStateOf(initialLongitude) }
//                if (locationPermissionsEnabled && !locationSet) {
//                    getLocation { location ->
//                        if (location != null) {
//                            latitude = location.latitude
//                            longitude = location.longitude
//                            locationSet = true
//                        }
//                    }
//                }
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Latitude")
                        OutlinedTextField(
                            value = String.format("%.2f", latitude),
                            onValueChange = { latitude = validateLatitude(it.toDouble()) },
                        )
                    }
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Longitude")
                        OutlinedTextField(
                            value = String.format("%.2f", longitude),
                            onValueChange = { longitude = validateLongitude(it.toDouble()) },
                        )
                    }
                }
                var radius by rememberSaveable { mutableStateOf(initialRadius) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Radius (km)")
                        OutlinedTextField(
                            value = String.format("%.1f", radius / 1000),
                            onValueChange = { radius = validateRadius(it.toFloat() * 1000) },
                        )
                    }
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    TextButton(onClick = {
                        showLocationSelectorDialog = true
                    }) {
                        Text("Set Location & Radius")
                    }
                }
                if (showLocationSelectorDialog) {
                    LocationSelector(
                        locationSelectedCallback = { coords, incomingRadius ->
                            latitude = coords.latitude
                            longitude = coords.longitude
                            radius = incomingRadius
                            showLocationSelectorDialog = false
                        },
                        initialLatitude = latitude,
                        initialLongitude = longitude,
                        initialRadius = radius,
                        locationPermissionsEnabled = locationPermissionsEnabled,
                        onDismissRequest = { showLocationSelectorDialog = false }
                    )
                }
                var listOfLabelsSelected by rememberSaveable {
                    mutableStateOf(initialLabelOptions)
                }
                Column {
                    Text("Labels")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (listOfLabelsSelected.size > 0) LazyRow(modifier = Modifier.weight(1f)) {
                            itemsIndexed(items = listOfLabelsSelected) { index: Int, item: LabelOptions ->
                                AssistChip(
                                    onClick = {
                                        listOfLabelsSelected =
                                            listOfLabelsSelected.filter { label -> label != item }
                                    },
                                    label = { Text(item.text) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove label",
                                            Modifier.size(InputChipDefaults.AvatarSize)
                                        )
                                    },
                                    modifier = Modifier.padding(
                                        start = if (index != 0) dimensionResource(
                                            R.dimen.padding_small
                                        ) else 0.dp
                                    )
                                )
                            }
                        } else {
                            Text("No labels selected")
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        var expanded by rememberSaveable { mutableStateOf(false) }

                        if (listOfLabelsSelected.size < enumValues<LabelOptions>().size) Box {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Filled.ArrowDropDown, "Expand/Collapse Label Dropdown")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                for (enumValue in enumValues<LabelOptions>().filter {
                                    !listOfLabelsSelected.contains(
                                        it
                                    )
                                }) {
                                    DropdownMenuItem(
                                        text = { Text(text = enumValue.text) },
                                        onClick = {
                                            expanded = false
                                            listOfLabelsSelected = listOfLabelsSelected + enumValue
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                // TODO move camera stuff into view model?
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = {
                            imageBitmap = null
                            imageUri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".provider",
                                createImageFile(context)
                            )
                            imageFromCameraLauncher.launch(imageUri)
                        }
                    ) {
                        Text("Take Picture")
                    }
                    TextButton(
                        onClick = {
                            imageFromGalleryLauncher.launch(
                                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text("Import Picture")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Display button to remove current image.
                    if ((imageUri != null) && (imageUri != Uri.EMPTY)) {
                        TextButton(
                            onClick = {
                                imageUri = null
                                imageBitmap = null
                            }
                        ) {
                            Text("Remove Picture")
                        }
                    }
                }

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
                            updateTripInDb(
                                name,
                                description,
                                if (startDatePickerState.selectedDateMillis != null) {
                                    convertToLocalDate(startDatePickerState)
                                } else {
                                    LocalDate.now()
                                },
                                if (endDatePickerState.selectedDateMillis != null) {
                                    convertToLocalDate(endDatePickerState)
                                } else {
                                    LocalDate.now()
                                },
                                latitude,
                                longitude,
                                radius,
                                daysBeforeToRemind,
                                getImageUriAsString(imageUri),
                                listOfLabelsSelected
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}



