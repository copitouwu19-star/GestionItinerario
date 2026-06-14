package com.gestion.itinerario.data.remote

import com.google.gson.annotations.SerializedName
import com.gestion.itinerario.data.entity.IntervalUnit
import com.gestion.itinerario.data.entity.MaintenanceReminder

data class MantenimientoRemoto(
    val id: String = "",
    @SerializedName("tecnico_uid")      val tecnicoUid: String = "",
    @SerializedName("cliente_id")       val clienteId: String = "",
    val equipo: String = "",
    val estado: String = "PENDING",
    val notas: String = "",
    @SerializedName("intervalo_valor")  val intervaloValor: Int = 3,
    @SerializedName("intervalo_unidad") val intervaloUnidad: String = "MONTHS",
    @SerializedName("fecha_ultimo")     val fechaUltimo: Long = 0L,
    @SerializedName("fecha_proximo")    val fechaProximo: Long = 0L,
    @SerializedName("fecha_registro")   val fechaRegistro: Long = 0L
) {
    fun toDomain() = MaintenanceReminder(
        id = id,
        equipmentId = "",
        equipmentType = equipo,
        clientId = clienteId,
        intervalValue = intervaloValor,
        intervalUnit = runCatching { IntervalUnit.valueOf(intervaloUnidad) }.getOrDefault(IntervalUnit.MONTHS),
        intervalMonths = intervaloValor,
        lastServiceDate = fechaUltimo,
        nextServiceDate = fechaProximo,
        notes = notas,
        isActive = true,
        source = "MANUAL",
        workStatus = estado
    )
}

fun MaintenanceReminder.toRemoto(uid: String) = MantenimientoRemoto(
    id = id,
    tecnicoUid = uid,
    clienteId = clientId,
    equipo = equipmentType,
    estado = workStatus,
    notas = notes,
    intervaloValor = intervalValue,
    intervaloUnidad = intervalUnit.name,
    fechaUltimo = lastServiceDate,
    fechaProximo = nextServiceDate,
    fechaRegistro = if (id.isEmpty()) System.currentTimeMillis() else 0L
)

data class MantenimientoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MantenimientoRemoto? = null
)

data class MantenimientoListResponse(
    val success: Boolean,
    val data: List<MantenimientoRemoto> = emptyList()
)
