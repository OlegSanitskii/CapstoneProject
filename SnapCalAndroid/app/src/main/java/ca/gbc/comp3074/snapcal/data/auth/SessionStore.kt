package ca.gbc.comp3074.snapcal.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    private object Keys {
        val LOGGED_IN   = booleanPreferencesKey("logged_in")
        val USER_EMAIL  = stringPreferencesKey("user_email")
        val USER_ID     = intPreferencesKey("user_id")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    /** Autologin (optional) rememberMe=true) */
    val isLoggedIn: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.LOGGED_IN] ?: false }

    val userEmail: Flow<String> =
        context.dataStore.data.map { it[Keys.USER_EMAIL] ?: "" }

    val userId: Flow<Int?> =
        context.dataStore.data.map { if (it.contains(Keys.USER_ID)) it[Keys.USER_ID] else null }

    val rememberMe: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.REMEMBER_ME] ?: false }


    suspend fun setUser(userId: Int?, email: String?, remember: Boolean = false) {
        context.dataStore.edit {
            it[Keys.LOGGED_IN] = remember
            it[Keys.REMEMBER_ME] = remember
            it[Keys.USER_EMAIL] = email.orEmpty()
            if (userId != null) {
                it[Keys.USER_ID] = userId
            } else {
                it.remove(Keys.USER_ID)
            }
        }
    }

    /** Sign Out */
    suspend fun signOut() {
        context.dataStore.edit {
            it[Keys.LOGGED_IN] = false
            it[Keys.REMEMBER_ME] = false
            it[Keys.USER_EMAIL] = ""
            it.remove(Keys.USER_ID)
        }
    }
}
