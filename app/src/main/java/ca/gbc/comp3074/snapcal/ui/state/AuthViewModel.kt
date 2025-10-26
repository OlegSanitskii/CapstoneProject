package ca.gbc.comp3074.snapcal.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.snapcal.data.auth.AuthRepository
import ca.gbc.comp3074.snapcal.data.auth.SessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository.get(app)
    private val session = SessionStore(app)

    val isLoggedIn = session.isLoggedIn.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val userEmail  = session.userEmail.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val userId     = session.userId.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun signIn(email: String, password: String, remember: Boolean, onDone: (Throwable?) -> Unit) {
        viewModelScope.launch {
            onDone(repo.signIn(email, password, remember).exceptionOrNull())
        }
    }

    fun signUp(email: String, password: String, name: String?, remember: Boolean, onDone: (Throwable?) -> Unit) {
        viewModelScope.launch {
            onDone(repo.signUp(email, password, name, remember).exceptionOrNull())
        }
    }

    fun continueAsGuest(onDone: () -> Unit) {
        viewModelScope.launch { repo.continueAsGuest(); onDone() }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch { repo.signOut(); onDone() }
    }
}
