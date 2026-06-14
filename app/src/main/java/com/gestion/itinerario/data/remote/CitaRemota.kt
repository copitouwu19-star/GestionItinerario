package com.gestion.itinerario.data.remote

import com.google.gson.annotations.SerializedName
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.ServiceType

data class CitaRemota(
    val id: String = "",
    @SerializedName("tecnico_uid")        val tecnicoUid: String = "",
    @SerializedName("cliente_id")         val clienteId: String = "",
    @SerializedName("tipo_servicio")      val tipoServicio: String = "MAINTENANCE",
    val equipo: String = "",
    val descripcion: String = "",
    val diagnostico: String = "",
    val estado: String = "SCHEDULED",
    val notas: String = "",
    @SerializedName("costo_total")        val costoTotal: Double = 0.0,
    @SerializedName("metodo_pago")        val metodoPago: String = "NONE",
    @SerializedName("estado_pago")        val estadoPago: String = "NONE",
    @SerializedName("fecha_cita")         val fechaCita: Long = 0L,
    @SerializedName("fecha_registro")     val fechaRegistro: Long = 0L,
    @SerializedName("fecha_finalizacion") val fechaFinalizacion: Long? = null
) {
    fun toDomain() = Appointment(
        id = id,
        clientId = clienteId,
        equipmentId = null,
        dateTime = fechaCita,
        serviceType = runCatching { ServiceType.valueOf(tipoServicio) }.getOrDefault(ServiceType.MAINTENANCE),
        status = runCatching { AppointmentStatus.valueOf(estado) }.getOrDefault(AppointmentStatus.SCHEDULED),
        notes = notas,
        equipmentType = equipo,
        createdAt = fechaRegistro,
        completedAt = fechaFinalizacion,
        attendanceStatus = ""
    )
}

fun Appointment.toRemota(uid: String) = CitaRemota(
    id = id,
    tecnicoUid = uid,
    clienteId = clientId,
    tipoServicio = serviceType.name,
    equipo = equipmentType,
    descripcion = notes,
    diagnostico = "",
    estado = status.name,
    notas = notes,
    costoTotal = 0.0,
    metodoPago = "NONE",
    estadoPago = "NONE",
    fechaCita = dateTime,
    fechaRegistro = createdAt,
    fechaFinalizacion = completedAt
)

data class CitaResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CitaRemota? = null
)

data class CitaListResponse(
    val success: Boolean,
    val data: List<CitaRemota> = emptyList()
)
