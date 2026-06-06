package com.gestion.itinerario.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gestion.itinerario.data.entity.CompanyProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val doc get() = db.collection("users").document(uid).collection("profile").document("company")

    fun getProfile(): Flow<CompanyProfile> = callbackFlow {
        if (uid.isEmpty()) { trySend(CompanyProfile()); close(); return@callbackFlow }
        val reg = doc.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val p = if (snap != null && snap.exists()) {
                CompanyProfile(
                    companyName    = snap.getString("companyName") ?: "",
                    ownerName      = snap.getString("ownerName") ?: "",
                    phone          = snap.getString("phone") ?: "",
                    email          = snap.getString("email") ?: "",
                    address        = snap.getString("address") ?: "",
                    logoUrl        = snap.getString("logoUrl") ?: "",
                    invoicePrefix  = snap.getString("invoicePrefix") ?: "FAC",
                    invoiceCounter = snap.getLong("invoiceCounter")?.toInt() ?: 0
                )
            } else CompanyProfile()
            trySend(p)
        }
        awaitClose { reg.remove() }
    }

    suspend fun save(profile: CompanyProfile) {
        doc.set(mapOf(
            "companyName"    to profile.companyName,
            "ownerName"      to profile.ownerName,
            "phone"          to profile.phone,
            "email"          to profile.email,
            "address"        to profile.address,
            "logoUrl"        to profile.logoUrl,
            "invoicePrefix"  to profile.invoicePrefix,
            "invoiceCounter" to profile.invoiceCounter
        )).await()
    }

    suspend fun uploadLogo(uri: Uri): String {
        val ref = storage.reference.child("users/$uid/logo.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun incrementCounter(): Int {
        val snap = doc.get().await()
        val current = snap.getLong("invoiceCounter")?.toInt() ?: 0
        val next = current + 1
        doc.update("invoiceCounter", next).await()
        return next
    }
}
