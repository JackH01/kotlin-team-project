package com.tripwizard

import android.app.Application
import com.tripwizard.data.AppContainer
import com.tripwizard.data.AppDataContainer


class TripWizardApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}