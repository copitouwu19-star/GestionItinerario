package com.gestion.itinerario.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.CompanyProfile
import com.gestion.itinerario.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    val profile: StateFlow<CompanyProfile> = repo.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompanyProfile())

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun save(p: CompanyProfile) = viewModelScope.launch { repo.save(p) }

    fun uploadLogoAndSave(uri: Uri) = viewModelScope.launch {
        _uploadState.value = UploadState.Loading
        try {
            val url = repo.uploadLogo(uri)
            repo.save(profile.value.copy(logoUrl = url))
            _uploadState.value = UploadState.Success
        } catch (e: Exception) {
            e.printStackTrace()
            _uploadState.value = UploadState.Error("No se pudo subir el logo")
        }
    }

    fun clearUploadState() { _uploadState.value = UploadState.Idle }
}

sealed class UploadState {
    object Idle    : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}
