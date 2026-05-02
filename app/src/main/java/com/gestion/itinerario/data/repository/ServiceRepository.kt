package com.gestion.itinerario.data.repository

import com.gestion.itinerario.data.db.ServiceOrderDao
import com.gestion.itinerario.data.db.ServiceSparePartDao
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceSparePart
import com.gestion.itinerario.data.entity.ServiceStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val orderDao: ServiceOrderDao,
    private val sparePartDao: ServiceSparePartDao
) {
    fun getAll(): Flow<List<ServiceOrder>> = orderDao.getAll()
    fun getByEquipment(id: Long): Flow<List<ServiceOrder>> = orderDao.getByEquipment(id)
    fun getByClient(id: Long): Flow<List<ServiceOrder>> = orderDao.getByClient(id)
    fun getByStatus(status: ServiceStatus): Flow<List<ServiceOrder>> = orderDao.getByStatus(status)
    fun countActive(): Flow<Int> = orderDao.countActive()
    fun countTotal(): Flow<Int> = orderDao.countTotal()
    fun getSpareParts(orderId: Long): Flow<List<ServiceSparePart>> = sparePartDao.getByOrder(orderId)
    suspend fun getById(id: Long): ServiceOrder? = orderDao.getById(id)
    suspend fun save(order: ServiceOrder) = orderDao.insert(order)
    suspend fun update(order: ServiceOrder) = orderDao.update(order)
    suspend fun delete(order: ServiceOrder) = orderDao.delete(order)
    suspend fun insertSparePart(item: ServiceSparePart) = sparePartDao.insert(item)
    suspend fun clearSpareParts(orderId: Long) = sparePartDao.deleteByOrder(orderId)
}
