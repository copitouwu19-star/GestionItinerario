package com.gestion.itinerario

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gestion.itinerario.workers.AppointmentReminderWorker
import com.gestion.itinerario.workers.MaintenanceReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GestionApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        val wm = WorkManager.getInstance(this)

        // Check appointments every day
        val appointmentWork = PeriodicWorkRequestBuilder<AppointmentReminderWorker>(1, TimeUnit.DAYS).build()
        wm.enqueueUniquePeriodicWork(
            AppointmentReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            appointmentWork
        )

        // Check maintenance every day
        val maintenanceWork = PeriodicWorkRequestBuilder<MaintenanceReminderWorker>(1, TimeUnit.DAYS).build()
        wm.enqueueUniquePeriodicWork(
            MaintenanceReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            maintenanceWork
        )
    }
}
