package com.tripwizard.ui.home

import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tripwizard.R
import com.tripwizard.data.TripWithAttractionsAndLabels

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ImageViewer(
    tripWithAttractionsAndLabels: TripWithAttractionsAndLabels,
    onDismissRequest: () -> Unit
) {

    BackHandler {
        onDismissRequest()
    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
//                .fillMaxSize()
                .padding(vertical = 64.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.dialog_elevation))
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                CenterAlignedTopAppBar(
                    title = { Text(tripWithAttractionsAndLabels.trip.name) },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button)
                            )
                        }
                    }
                )
                Image(
                    getImageBitmapFromUri(
                        Uri.parse(tripWithAttractionsAndLabels.trip.imageUri),
                        LocalContext.current
                    ),
                    modifier = Modifier.wrapContentSize(),
                    contentDescription = "Image for ${tripWithAttractionsAndLabels.trip.name} trip",
                )
//                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Close")
                }
            }
        }
    }
}
