package com.gestion.itinerario.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    val paletteId: StateFlow<String> = prefsRepo.colorPalette
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "indigo")

    fun setPalette(id: String) = viewModelScope.launch { prefsRepo.setColorPalette(id) }
}
