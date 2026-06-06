package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.Client
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val col get() = db.collection("users").document(uid).collection("clients")

    fun getAll(): Flow<List<Client>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = col.orderBy("name").addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toClient() } ?: emptyList())
        }
        awaitClose { reg.remove() }
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

    suspend fun getById(id: String): Client? = col.document(id).get().await().toClient()

    suspend fun save(client: Client): String {
        val ref = if (client.id.isEmpty()) col.document() else col.document(client.id)
        ref.set(client.toMap()).await()
        return ref.id
    }

    suspend fun update(client: Client) { col.document(client.id).set(client.toMap()).await() }
    suspend fun delete(client: Client) { col.document(client.id).delete().await() }
}

private fun Client.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "lastName" to lastName,
    "phone" to phone,
    "email" to email,
    "address" to address,
    "notes" to notes,
    "clientType" to clientType,
    "createdAt" to createdAt
)

private fun com.google.firebase.firestore.DocumentSnapshot.toClient(): Client? {
    if (!exists()) return null
    return try {
        Client(
            id = id,
            name = getString("name") ?: "",
            lastName = getString("lastName") ?: "",
            phone = getString("phone") ?: "",
            email = getString("email") ?: "",
            address = getString("address") ?: "",
            notes = getString("notes") ?: "",
            clientType = getString("clientType") ?: "Persona Natural",
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) { null }
}
