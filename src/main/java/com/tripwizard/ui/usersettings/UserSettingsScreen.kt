package com.tripwizard.ui.usersettings


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripwizard.R
import com.tripwizard.ui.AppViewModelProvider
import com.tripwizard.ui.components.TripWizardTopAppBar
import com.tripwizard.ui.components.TripWizardBottomNavigationBar
import com.tripwizard.ui.navigation.NavigationDestination
import com.tripwizard.ui.utils.handleNotifications

@Composable
fun UserSettingsScreen(
    darkModePreferredListener: (Boolean?) -> Unit,
    isDarkMode: Boolean,
    onNavigate: (String) -> Unit,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val usernameUiState by viewModel.usernameUiState.collectAsState()
    val darkModePreferredUiState by viewModel.darkModePreferredUiState.collectAsState()
    darkModePreferredListener(darkModePreferredUiState.data.darkModePreferred)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val tripListUiState by viewModel.tripListUiState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    handleNotifications(
        scope = scope,
        snackbarHostState = snackbarHostState,
        notificationsUiState = viewModel.notificationsUiState,
        updateNotification = viewModel::updateNotification,
        insertNotifications = viewModel::insertNotifications,
        tripsWithAttractionsAndLabelsList = tripListUiState.tripWithAttractionsAndLabelsList
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TripWizardTopAppBar(
                title = stringResource(NavigationDestination.SETTINGS.titleRes),
                canNavigateBack = true,
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
                notificationsUiState = viewModel.notificationsUiState,
                tripsWithAttractionsAndLabelsList = tripListUiState.tripWithAttractionsAndLabelsList,
                updateNotification = viewModel::updateNotification,
            )
        },
        bottomBar = {
            TripWizardBottomNavigationBar(
                currentRoute = NavigationDestination.SETTINGS,
                navigate = onNavigate
            )
        }) { innerPadding ->
        if (!usernameUiState.isLoading && !darkModePreferredUiState.isLoading) {
            var localUsernameState by rememberSaveable {
                mutableStateOf(usernameUiState.data.username)
            }

            if (localUsernameState != usernameUiState.data.username) {
                LaunchedEffect(localUsernameState) {
                    viewModel.updateUsername(localUsernameState)
                }
            }

            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(dimensionResource(id = R.dimen.padding_medium))
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
            ) {
                Column {
                    val focusManager = LocalFocusManager.current

                    Text("Username")
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                    OutlinedTextField(
                        value = localUsernameState,
                        onValueChange = { localUsernameState = it },
                        placeholder = { Text("Enter Username…") },
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth())
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Dark Mode")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.updateDarkModePreferred(it) }
                    )
                }
            }
        } else {
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
    }
}