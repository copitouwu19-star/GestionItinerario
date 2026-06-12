package com.gestion.itinerario.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.CompanyProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context
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
                    taxId          = snap.getString("taxId") ?: "",
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
            "taxId"          to profile.taxId,
            "ownerName"      to profile.ownerName,
            "phone"          to profile.phone,
            "email"          to profile.email,
            "address"        to profile.address,
            "logoUrl"        to profile.logoUrl,
            "invoicePrefix"  to profile.invoicePrefix,
            "invoiceCounter" to profile.invoiceCounter
        )).await()
    }

    /** Lee la imagen, la escala a máx 400×400 px, la comprime a JPEG 80% y devuelve un data URI Base64.
     *  El resultado se guarda en Firestore como `logoUrl` — no requiere Firebase Storage. */
    suspend fun uploadLogo(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo leer la imagen seleccionada")
        val original = BitmapFactory.decodeStream(inputStream)
            ?: throw Exception("El archivo seleccionado no es una imagen válida")

        val maxPx = 400
        val scale = minOf(maxPx.toFloat() / original.width, maxPx.toFloat() / original.height, 1f)
        val scaled = if (scale < 1f)
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt(),
                (original.height * scale).toInt(),
                true
            )
        else original

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$b64"
    }

    suspend fun incrementCounter(): Int {
        // 1. Leer el contador (caché primero, luego red, con timeouts cortos)
        val snap = withTimeoutOrNull(2_000L) {
            try { doc.get(com.google.firebase.firestore.Source.CACHE).await() } catch (_: Exception) { null }
        } ?: withTimeoutOrNull(3_000L) {
            try { doc.get().await() } catch (_: Exception) { null }
        }
        val current = snap?.getLong("invoiceCounter")?.toInt() ?: 0
        val next = current + 1
        // 2. Escribir el nuevo valor; si hay red lenta Firestore lo sincroniza en background
        try {
            withTimeout(4_000L) {
                doc.set(
                    mapOf("invoiceCounter" to next),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
            }
        } catch (_: Exception) { /* sincronizará cuando haya red */ }
        return next
    }
}
