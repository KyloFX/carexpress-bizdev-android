package com.carexpress.bizdev.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carexpress.bizdev.data.VisitRepository
import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.data.local.VisitEntity
import com.carexpress.bizdev.data.local.VisitType
import com.carexpress.bizdev.data.seed.TargetDirectory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class VisitForm(
    val id: Long = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val timestampOverridden: Boolean = false,
    val organisation: String = "",
    val segment: Segment? = null,
    val keyPerson: String = "",
    val role: String = "",
    val visitType: VisitType = VisitType.IN_PERSON,
    val stage: Stage = Stage.INITIAL_CONTACT,
    val coveredLives: String = "",
    val monthlyValue: String = "",
    val leadScore: Int = 3,
    val notes: String = "",
    val nextAction: String = "",
    val followUpDate: LocalDate? = null,
    val createdAt: LocalDateTime? = null,
    val showErrors: Boolean = false,
) {
    val isEdit get() = id != 0L
    val orgError get() = organisation.isBlank()
    val segmentError get() = segment == null
    val isValid get() = !orgError && !segmentError
}

@HiltViewModel
class VisitEditorViewModel @Inject constructor(private val repo: VisitRepository) : ViewModel() {

    private val _form = MutableStateFlow(VisitForm())
    val form: StateFlow<VisitForm> = _form.asStateFlow()

    /** Directory orgs + any custom org names already logged. */
    val organisationSuggestions: StateFlow<List<String>> = repo.knownOrganisations
        .map { (TargetDirectory.organisations.keys + it).distinct() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TargetDirectory.organisations.keys.toList())

    /** Called when the sheet opens for a new visit: auto-stamps the current time. */
    fun startNew() { _form.value = VisitForm(timestamp = LocalDateTime.now()) }

    fun startEdit(v: VisitEntity) {
        _form.value = VisitForm(
            id = v.id, timestamp = v.visitTimestamp, timestampOverridden = true, organisation = v.organisation,
            segment = v.segment, keyPerson = v.keyPerson, role = v.role, visitType = v.visitType, stage = v.stage,
            coveredLives = if (v.coveredLives == 0) "" else v.coveredLives.toString(),
            monthlyValue = if (v.monthlyValue == 0.0) "" else v.monthlyValue.toBigDecimal().stripTrailingZeros().toPlainString(),
            leadScore = v.leadScore, notes = v.notes, nextAction = v.nextAction, followUpDate = v.followUpDate,
            createdAt = v.createdAt,
        )
    }

    fun update(block: VisitForm.() -> VisitForm) = _form.update(block)

    fun setOrganisation(name: String) = _form.update {
        val known = TargetDirectory.organisations[name]
        it.copy(organisation = name, segment = known ?: it.segment)
    }

    fun setTimestamp(ts: LocalDateTime) = _form.update { it.copy(timestamp = ts, timestampOverridden = true) }

    /** Returns true when saved. */
    fun save(onDone: () -> Unit) {
        val f = _form.value
        if (!f.isValid) { _form.update { it.copy(showErrors = true) }; return }
        viewModelScope.launch {
            repo.save(
                VisitEntity(
                    id = f.id,
                    organisation = f.organisation.trim(),
                    segment = f.segment!!,
                    keyPerson = f.keyPerson.trim(),
                    role = f.role.trim(),
                    visitType = f.visitType,
                    stage = f.stage,
                    coveredLives = f.coveredLives.filter(Char::isDigit).toIntOrNull() ?: 0,
                    monthlyValue = f.monthlyValue.toDoubleOrNull() ?: 0.0,
                    leadScore = f.leadScore,
                    notes = f.notes.trim(),
                    nextAction = f.nextAction.trim(),
                    followUpDate = f.followUpDate,
                    visitTimestamp = f.timestamp,
                    createdAt = f.createdAt ?: LocalDateTime.now(),
                )
            )
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = _form.value.id
        if (id == 0L) return
        viewModelScope.launch {
            repo.get(id)?.let { repo.delete(it) }
            onDone()
        }
    }
}
