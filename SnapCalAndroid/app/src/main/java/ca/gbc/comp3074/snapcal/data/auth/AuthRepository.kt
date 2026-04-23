package ca.gbc.comp3074.snapcal.data.auth

import android.content.Context
import ca.gbc.comp3074.snapcal.data.user.UserRepository
import kotlinx.coroutines.delay

class AuthRepository private constructor(
    private val session: SessionStore,
    private val users: UserRepository
) {

    /**
     * Authorization
     *  remember=true → save flag of autoLogIn in DataStore.
     */
    suspend fun signIn(email: String, password: String, remember: Boolean): Result<Unit> {
        delay(200)
        val res = users.login(email, password)
        return res.map { user ->
            session.setUser(
                userId = user.id,
                email = user.email,
                remember = remember
            )
        }
    }

    /**
     * New user registration.
     * Automatic Log In.
     */
    suspend fun signUp(email: String, password: String, name: String?, remember: Boolean): Result<Unit> {
        val res = users.register(email, password, name)
        return res.map { newId ->
            session.setUser(
                userId = newId,
                email = email,
                remember = remember
            )
        }
    }

    /**
     * Guest (No session saved).
     */
    suspend fun continueAsGuest() {
        session.setUser(userId = null, email = null, remember = false)
    }

    /**
     * Sign Out
     */
    suspend fun signOut() = session.signOut()

    companion object {
        @Volatile
        private var inst: AuthRepository? = null

        fun get(context: Context): AuthRepository =
            inst ?: synchronized(this) {
                val appCtx = context.applicationContext
                val repo = AuthRepository(
                    session = SessionStore(appCtx),
                    users = UserRepository.get(appCtx)
                )
                inst = repo
                repo
            }
    }
}
