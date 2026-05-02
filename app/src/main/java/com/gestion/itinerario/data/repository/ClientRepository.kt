package com.gestion.itinerario.data.repository

import com.gestion.itinerario.data.db.ClientDao
import com.gestion.itinerario.data.entity.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(private val dao: ClientDao) {
    fun getAll(): Flow<List<Client>> = dao.getAll()
    fun search(query: String): Flow<List<Client>> = dao.search(query)
    suspend fun getById(id: Long): Client? = dao.getById(id)
    suspend fun save(client: Client): Long = dao.insert(client)
    suspend fun update(client: Client) = dao.update(client)
    suspend fun delete(client: Client) = dao.delete(client)
}
