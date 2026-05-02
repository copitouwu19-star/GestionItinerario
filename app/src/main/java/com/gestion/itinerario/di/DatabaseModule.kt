package com.gestion.itinerario.di

import android.content.Context
import androidx.room.Room
import com.gestion.itinerario.data.db.AppDatabase
import com.gestion.itinerario.data.db.AppointmentDao
import com.gestion.itinerario.data.db.ClientDao
import com.gestion.itinerario.data.db.EquipmentDao
import com.gestion.itinerario.data.db.MaintenanceReminderDao
import com.gestion.itinerario.data.db.ServiceOrderDao
import com.gestion.itinerario.data.db.ServiceSparePartDao
import com.gestion.itinerario.data.db.SparePartDao
import com.gestion.itinerario.data.db.StockMovementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "gestion_itinerario.db").build()

    @Provides fun provideEquipmentDao(db: AppDatabase): EquipmentDao = db.equipmentDao()
    @Provides fun provideClientDao(db: AppDatabase): ClientDao = db.clientDao()
    @Provides fun provideSparePartDao(db: AppDatabase): SparePartDao = db.sparePartDao()
    @Provides fun provideStockMovementDao(db: AppDatabase): StockMovementDao = db.stockMovementDao()
    @Provides fun provideServiceOrderDao(db: AppDatabase): ServiceOrderDao = db.serviceOrderDao()
    @Provides fun provideServiceSparePartDao(db: AppDatabase): ServiceSparePartDao = db.serviceSparePartDao()
    @Provides fun provideAppointmentDao(db: AppDatabase): AppointmentDao = db.appointmentDao()
    @Provides fun provideMaintenanceReminderDao(db: AppDatabase): MaintenanceReminderDao = db.maintenanceReminderDao()
}
