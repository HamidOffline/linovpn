package ir.linovpn.app.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.linovpn.app.domain.model.Profile
import ir.linovpn.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProfilesUiState(
    val profiles: List<Profile> = emptyList(),
    val updatingProfileIds: Set<String> = emptySet(),
    val isUpdatingAll: Boolean = false,
    val showAddDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val updatingProfileIds = MutableStateFlow<Set<String>>(emptySet())
    private val isUpdatingAll = MutableStateFlow(false)
    private val showAddDialog = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfilesUiState> = combine(
        profileRepository.getAllProfiles(),
        updatingProfileIds,
        isUpdatingAll,
        showAddDialog,
        errorMessage
    ) { profiles, updatingIds, updatingAll, showDialog, error ->
        ProfilesUiState(
            profiles = profiles,
            updatingProfileIds = updatingIds,
            isUpdatingAll = updatingAll,
            showAddDialog = showDialog,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfilesUiState()
    )

    fun openAddDialog() {
        showAddDialog.value = true
    }

    fun closeAddDialog() {
        showAddDialog.value = false
    }

    fun addProfile(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) return
        viewModelScope.launch {
            val newProfile = Profile(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                subscriptionUrl = url.trim(),
                lastUpdated = 0L
            )
            profileRepository.insertProfile(newProfile)
            showAddDialog.value = false
            updateProfile(newProfile)
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            updatingProfileIds.value = updatingProfileIds.value + profile.id
            val result = profileRepository.fetchAndSaveSubscription(profile)
            if (result.isFailure) {
                errorMessage.value = result.exceptionOrNull()?.message ?: "خطا در بروزرسانی اشتراک"
            }
            updatingProfileIds.value = updatingProfileIds.value - profile.id
        }
    }

    fun updateAll() {
        viewModelScope.launch {
            isUpdatingAll.value = true
            val list = uiState.value.profiles
            for (p in list) {
                updateProfile(p)
            }
            isUpdatingAll.value = false
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile.id)
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}
