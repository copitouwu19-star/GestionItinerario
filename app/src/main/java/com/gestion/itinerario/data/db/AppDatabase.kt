package com.gestion.itinerario.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.Equipment
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceSparePart
import com.gestion.itinerario.data.entity.SparePart
import com.gestion.itinerario.data.entity.StockMovement

@Database(
    entities = [
        Equipment::class,
        Client::class,
        SparePart::class,
        StockMovement::class,
        ServiceOrder::class,
        ServiceSparePart::class,
        Appointment::class,
        MaintenanceReminder::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun clientDao(): ClientDao
    abstract fun sparePartDao(): SparePartDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun serviceOrderDao(): ServiceOrderDao
    abstract fun serviceSparePartDao(): ServiceSparePartDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun maintenanceReminderDao(): MaintenanceReminderDao
}
