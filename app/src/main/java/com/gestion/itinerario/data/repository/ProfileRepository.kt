package com.gestion.itinerario.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.firebase.auth.FirebaseAuth
import com.gestion.itinerario.data.entity.CompanyProfile
import com.gestion.itinerario.data.remote.PerfilApiService
import com.gestion.itinerario.data.remote.toRemoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: PerfilApiService,
    @ApplicationContext private val context: Context
) {
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _profile = MutableStateFlow(CompanyProfile())
    private var lastUid = ""

    fun getProfile(): Flow<CompanyProfile> = flow {
        val currentUid = uid
        if (currentUid.isNotEmpty() && currentUid != lastUid) {
            lastUid = currentUid
            loadFromServer()
        }
        emitAll(_profile)
    }

    suspend fun save(profile: CompanyProfile) {
        _profile.value = profile
        try { api.save(profile.toRemoto(uid)) } catch (_: Exception) {}
    }

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
        val next = _profile.value.invoiceCounter + 1
        val updated = _profile.value.copy(invoiceCounter = next)
        _profile.value = updated
        try { api.save(updated.toRemoto(uid)) } catch (_: Exception) {}
        return next
    }

    private suspend fun loadFromServer() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        try {
            val resp = api.get(currentUid)
            if (resp.isSuccessful) {
                resp.body()?.data?.toDomain()?.let { _profile.value = it }
            }
        } catch (_: Exception) {}
    }
}
