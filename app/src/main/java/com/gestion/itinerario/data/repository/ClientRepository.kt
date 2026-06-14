package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.remote.ClientApiService
import com.gestion.itinerario.data.remote.toRemoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(private val api: ClientApiService) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    private var lastUid = ""

    fun getAll(): Flow<List<Client>> = flow {
        val currentUid = uid
        if (currentUid.isNotEmpty() && currentUid != lastUid) {
            lastUid = currentUid
            refresh()
        }
        emitAll(_clients)
    }

    fun search(query: String): Flow<List<Client>> = getAll().map { list ->
        val q = query.lowercase()
        list.filter {
            it.name.lowercase().contains(q) ||
            it.lastName.lowercase().contains(q) ||
            it.phone.contains(q) ||
            it.email.lowercase().contains(q)
        }
    }

    suspend fun getById(id: String): Client? {
        val cached = _clients.value.firstOrNull { it.id == id }
        if (cached != null) return cached
        refresh()
        return _clients.value.firstOrNull { it.id == id }
    }

    suspend fun save(client: Client): String {
        val id = client.id.ifEmpty { UUID.randomUUID().toString() }
        val c = client.copy(id = id)
        try { api.save(c.toRemoto(uid)) } catch (_: Exception) {}
        val list = _clients.value.toMutableList().apply {
            removeAll { it.id == id }
            add(c)
        }
        _clients.value = list.sortedBy { it.name }
        return id
    }

    suspend fun update(client: Client) {
        try { api.update(client.id, client.toRemoto(uid)) } catch (_: Exception) {}
        _clients.value = _clients.value.map { if (it.id == client.id) client else it }
    }

    suspend fun delete(client: Client) {
        try { api.delete(client.id) } catch (_: Exception) {}
        _clients.value = _clients.value.filter { it.id != client.id }
    }

    suspend fun refresh() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        try {
            val resp = api.getAll(currentUid)
            if (resp.isSuccessful) {
                _clients.value = resp.body()?.data?.map { it.toDomain() }
                    ?.sortedBy { it.name } ?: emptyList()
            }
        } catch (_: Exception) {}
    }
}
