package ca.gbc.comp3074.snapcal.data.user

import android.content.Context
import ca.gbc.comp3074.snapcal.data.db.DBProvider

class UserRepository private constructor(private val dao: UserDao) {

    suspend fun register(email: String, password: String, name: String?): Result<Int> {
        if (email.isBlank() || !email.contains("@")) return Result.failure(IllegalArgumentException("Invalid email"))
        if (password.length < 4) return Result.failure(IllegalArgumentException("Password too short"))
        if (dao.findByEmail(email) != null) return Result.failure(IllegalStateException("Email already registered"))
        val id = dao.insert(User(email = email.trim(), password = password, name = name)).toInt()
        return Result.success(id)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val user = dao.findByCredentials(email.trim(), password)
        return if (user != null) Result.success(user) else Result.failure(IllegalArgumentException("Wrong email or password"))
    }

    companion object {
        @Volatile private var inst: UserRepository? = null
        fun get(context: Context): UserRepository =
            inst ?: synchronized(this) {
                val db = DBProvider.get(context)
                UserRepository(db.userDao()).also { inst = it }
            }
    }
}
