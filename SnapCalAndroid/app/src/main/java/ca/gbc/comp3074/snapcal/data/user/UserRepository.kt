package ca.gbc.comp3074.snapcal.data.user

import android.content.Context
import ca.gbc.comp3074.snapcal.data.db.DBProvider

class UserRepository private constructor(private val dao: UserDao) {

    suspend fun register(email: String, password: String, name: String?): Result<Int> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Invalid email"))
        }

        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Password too short"))
        }

        val normalizedEmail = email.trim()

        if (dao.findByEmail(normalizedEmail) != null) {
            return Result.failure(IllegalStateException("Email already registered"))
        }

        val id = dao.insert(
            User(
                email = normalizedEmail,
                password = password,
                name = name,
                healthConnectEnabled = false,
                reportsEnabled = false
            )
        ).toInt()

        return Result.success(id)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val user = dao.findByCredentials(email.trim(), password)

        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Wrong email or password"))
        }
    }

    suspend fun getById(userId: Int): User? =
        dao.getById(userId)

    fun observeUser(userId: Int) =
        dao.observeUser(userId)

    suspend fun setHealthConnectEnabled(userId: Int, enabled: Boolean) =
        dao.setHealthConnectEnabled(userId, enabled)

    suspend fun setReportsEnabled(userId: Int, enabled: Boolean) =
        dao.setReportsEnabled(userId, enabled)

    companion object {
        @Volatile
        private var inst: UserRepository? = null

        fun get(context: Context): UserRepository =
            inst ?: synchronized(this) {
                val db = DBProvider.get(context)
                UserRepository(db.userDao()).also { inst = it }
            }
    }
}