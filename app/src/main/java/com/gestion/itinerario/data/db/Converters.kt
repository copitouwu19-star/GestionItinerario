package com.gestion.itinerario.data.db

import androidx.room.TypeConverter
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.EquipmentStatus
import com.gestion.itinerario.data.entity.MovementType
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.entity.ServiceType

class Converters {
    @TypeConverter fun fromEquipmentStatus(v: EquipmentStatus) = v.name
    @TypeConverter fun toEquipmentStatus(v: String) = EquipmentStatus.valueOf(v)

    @TypeConverter fun fromServiceType(v: ServiceType) = v.name
    @TypeConverter fun toServiceType(v: String) = ServiceType.valueOf(v)

    @TypeConverter fun fromServiceStatus(v: ServiceStatus) = v.name
    @TypeConverter fun toServiceStatus(v: String) = ServiceStatus.valueOf(v)

    @TypeConverter fun fromAppointmentStatus(v: AppointmentStatus) = v.name
    @TypeConverter fun toAppointmentStatus(v: String) = AppointmentStatus.valueOf(v)

    @TypeConverter fun fromMovementType(v: MovementType) = v.name
    @TypeConverter fun toMovementType(v: String) = MovementType.valueOf(v)
}
