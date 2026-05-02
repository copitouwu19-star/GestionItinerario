package com.gestion.itinerario.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.data.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repo: ReminderRepository
) : ViewModel() {
    val reminders: StateFlow<List<MaintenanceReminder>> = repo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(r: MaintenanceReminder) = viewModelScope.launch { repo.save(r) }
    fun delete(r: MaintenanceReminder) = viewModelScope.launch { repo.delete(r) }
    fun markDone(r: MaintenanceReminder) = viewModelScope.launch {
        val cal = Calendar.getInstance()
        val last = cal.timeInMillis
        cal.add(Calendar.MONTH, r.intervalMonths)
        repo.update(r.copy(lastServiceDate = last, nextServiceDate = cal.timeInMillis))
    }
}
