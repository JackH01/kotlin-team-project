package com.tripwizard.ui.utils

fun darkModePreferredListener(
    isDarkMode: Boolean,
    setIsDarkMode: (Boolean?) -> Unit
): (darkModePreferred: Boolean?) -> Unit = { darkModePreferred ->
    if (isDarkMode != darkModePreferred && darkModePreferred != null) {
        setIsDarkMode(darkModePreferred)
    }
}