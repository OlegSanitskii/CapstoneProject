package ca.gbc.comp3074.snapcal.data.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getById(userId: Int): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUser(userId: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun findByCredentials(email: String, password: String): User?

    @Query("UPDATE users SET healthConnectEnabled = :enabled WHERE id = :userId")
    suspend fun setHealthConnectEnabled(userId: Int, enabled: Boolean)

    @Query("UPDATE users SET reportsEnabled = :enabled WHERE id = :userId")
    suspend fun setReportsEnabled(userId: Int, enabled: Boolean)
}