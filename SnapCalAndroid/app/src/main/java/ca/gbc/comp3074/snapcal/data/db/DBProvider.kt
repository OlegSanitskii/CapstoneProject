package ca.gbc.comp3074.snapcal.data.db

import android.content.Context
import androidx.room.Room

object DBProvider {
    @Volatile private var inst: SnapCalDatabase? = null

    fun get(context: Context): SnapCalDatabase =
        inst ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                SnapCalDatabase::class.java,
                "snapcal.db"
            )

                .fallbackToDestructiveMigration()
                .build()
                .also { inst = it }
        }
}
